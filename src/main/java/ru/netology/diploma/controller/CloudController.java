package ru.netology.diploma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.dto.request.RenameFileRequestDto;
import ru.netology.diploma.pojo.exceptions.FileDownloadException;
import ru.netology.diploma.pojo.exceptions.FileUploadException;
import ru.netology.diploma.pojo.exceptions.FileRewriteException;
import ru.netology.diploma.pojo.exceptions.InputDataException;
import ru.netology.diploma.security.jwt.JwtTokenProvider;
import ru.netology.diploma.service.CloudDBService;
import ru.netology.diploma.service.FileUploadDownloadService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CloudController {
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudDBService cloudDBService;
    private final FileUploadDownloadService fileUploadDownloadService;

    @Autowired
    public CloudController(JwtTokenProvider jwtTokenProvider, CloudDBService cloudDBService, FileUploadDownloadService fileUploadDownloadService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cloudDBService = cloudDBService;
        this.fileUploadDownloadService = fileUploadDownloadService;
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) throws FileUploadException, FileRewriteException {

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));

        if (cloudDBService.getCurrentFile(username, filename) != null)
            throw new FileRewriteException("File with name " + filename + " already exists");

        fileUploadDownloadService.uploadFile(file, username, filename);
        cloudDBService.uploadFileToUserStorage(username, filename, file);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity downloadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename) throws IOException, FileDownloadException {

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        if (cloudDBService.getCurrentFile(username, filename) == null)
            throw new FileNotFoundException("File with name " + filename + " does not exist in your storage");

        MultipartFile multipartFile = fileUploadDownloadService.downloadFile(username, filename);

        byte[] body = multipartFile.getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(body));

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(multipartFile.getSize())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                //.contentType(MediaType.parseMediaType(multipartFile.getContentType()))
                .body(resource);
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename) throws InputDataException, IOException {

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        cloudDBService.deleteFileByUsernameAndFilename(username, filename);
        fileUploadDownloadService.deleteFile(username, filename);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @Valid @RequestBody RenameFileRequestDto newFilename) throws FileRewriteException {

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        cloudDBService.renameFile(username, filename, newFilename.getFilename());
        fileUploadDownloadService.renameFile(username, filename, newFilename.getFilename());

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileFromListResponseDto>> getList(
            HttpServletRequest request,
            @RequestParam("limit") int limit) {
        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        List<FileFromListResponseDto> filesFromList = cloudDBService.getLimitFilesByUsername(username, limit)
                .stream()
                .map((StorageFile storageFile) -> new FileFromListResponseDto(storageFile.getFilename(), storageFile.getFileSize()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(filesFromList);
    }
}
