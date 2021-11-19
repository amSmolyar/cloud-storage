package ru.netology.diploma.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.pojo.exceptions.*;
import ru.netology.diploma.service.CloudDBService;
import ru.netology.diploma.service.CloudFileService;
import ru.netology.diploma.service.CloudStorageService;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CloudStorageServiceImpl implements CloudStorageService {

    private final CloudDBService cloudDBService;
    private final CloudFileService cloudFileService;

    @Value("${storage.files.regex}")
    private final String filenameRegex = ".+\\.\\w+$";

    @Autowired
    public CloudStorageServiceImpl(CloudDBService cloudDBService, CloudFileService cloudFileService) {
        this.cloudDBService = cloudDBService;
        this.cloudFileService = cloudFileService;
    }

    @Override
    public void inputDataValidation(MultipartFile file) throws InputDataException {
        if (file == null || Objects.requireNonNull(file.getOriginalFilename()).isEmpty() || file.getSize() == 0)
            throw new InputDataException("Incorrect file input data");
    }

    @Override
    public void inputDataValidation(String filename) throws InputDataException {
        if (filename == null || !filename.matches(filenameRegex))
            throw new InputDataException("Incorrect input data (filename)");
    }

    @Override
    public void uploadFile(String username, String filename, MultipartFile file) throws FileRewriteException, FileUploadException {

        inputDataValidation(file);
        inputDataValidation(filename);

        if (cloudDBService.getCurrentFile(username, filename) != null)
            throw new FileRewriteException("File with name " + filename + " already exists");

        cloudFileService.uploadFile(file, username, filename);
        cloudDBService.uploadFileToUserStorage(username, filename, file);
    }

    @Override
    public MultipartFile downloadFile(String username, String filename) throws FileNotFoundException, FileDownloadException {
        inputDataValidation(filename);

        if (cloudDBService.getCurrentFile(username, filename) == null)
            throw new FileNotFoundException("File with name " + filename + " does not exist in your storage");

        return cloudFileService.downloadFile(username, filename);
    }

    @Override
    public void renameFile(String username, String filename, String newFilename) throws FileNotFoundException, FileRewriteException  {
        inputDataValidation(filename);

        if (cloudDBService.getCurrentFile(username, filename) != null) {
            try {
                cloudFileService.renameFile(username, filename, newFilename);
                cloudDBService.renameFile(username, filename, newFilename);
            } catch (FileNotFoundException e) {
                cloudDBService.deleteFileByUsernameAndFilename(username, filename);
                throw new FileNotFoundException(e.getMessage());
            }
        } else
            throw new InputDataException("File with name " + filename + " does not exist in storage");
    }

    @Override
    public void deleteFile(String username, String filename) throws FileDeleteException, FileNotFoundException {
        inputDataValidation(filename);

        if (cloudDBService.getCurrentFile(username, filename) != null) {
            try {
                cloudFileService.deleteFile(username, filename);
                cloudDBService.deleteFileByUsernameAndFilename(username, filename);
            } catch (FileNotFoundException e) {
                cloudDBService.deleteFileByUsernameAndFilename(username, filename);
                throw new FileNotFoundException(e.getMessage());
            }
        } else
            throw new InputDataException("File with name " + filename + " does not exist in storage");

    }

    @Override
    public List<FileFromListResponseDto> getLimitFileList(String username, int limit) {
        return cloudDBService.getLimitFilesByUsername(username, limit)
                .stream()
                .map((StorageFile storageFile) -> new FileFromListResponseDto(storageFile.getFilename(), storageFile.getFileSize()))
                .collect(Collectors.toList());
    }
}
