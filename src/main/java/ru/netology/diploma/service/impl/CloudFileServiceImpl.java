package ru.netology.diploma.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.pojo.exceptions.*;
import ru.netology.diploma.service.CloudFileService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CloudFileServiceImpl implements CloudFileService {

    @Value("${storage.upload.path}")
    private String uploadPath;

    @Override
    public void uploadFile(MultipartFile file, String username, String filename) throws FileUploadException {
        File uploadDir = new File(uploadPath + "/" + username);

        if (!uploadDir.exists())
            uploadDir.mkdirs();

        Path dir = Paths.get(uploadPath + "/" + username);
        Path filepath = Paths.get(dir.toString(), file.getOriginalFilename());

        try {
            file.transferTo(filepath);
        } catch (IOException | IllegalStateException e) {
            throw new FileUploadException("Can't upload file " + filename);
        }
    }

    @Override
    public MultipartFile downloadFile(String username, String filename) throws InputDataException, FileNotFoundException, FileDownloadException {

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);

        if (!Files.exists(path))
            throw new FileNotFoundException("File " + path + " not found");

        String name = filename;
        String originalFileName = filename;
        try {
            String contentType = Files.probeContentType(path);
            byte[] content = Files.readAllBytes(path);
            return new MockMultipartFile(name, originalFileName, contentType, content);
        } catch (IOException e) {
            throw new FileDownloadException("Can't download file " + filename);
        }
    }

    @Override
    public void renameFile(String username, String filename, String newFilename) throws FileRewriteException, FileNotFoundException {
        Path source = Paths.get(uploadPath + "/" + username + "/" + filename);

        if (!Files.exists(source))
            throw new FileNotFoundException("File " + source + " not found");

        try {
            Files.move(source, source.resolveSibling(newFilename));
        } catch (IOException e) {
            throw new FileRewriteException("Can't rename file");
        }
    }

    @Override
    public void deleteFile(String username, String filename) throws FileNotFoundException, FileDeleteException {
        Path source = Paths.get(uploadPath + "/" + username + "/" + filename);

        if (!Files.exists(source))
            throw new FileNotFoundException("File " + source + " not found");

        try {
            Files.delete(source);
        } catch (IOException e) {
            throw new FileDeleteException("Can't delete file");
        }
    }
}
