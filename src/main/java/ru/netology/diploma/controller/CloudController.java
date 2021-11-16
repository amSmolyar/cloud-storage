package ru.netology.diploma.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.dto.request.RenameFileRequestDto;
import ru.netology.diploma.pojo.exceptions.*;
import ru.netology.diploma.service.AssistantService;
import ru.netology.diploma.service.CloudStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.util.List;

@RestController
@Slf4j
public class CloudController {

    private final AssistantService assistantService;
    private final CloudStorageService cloudStorageService;

    @Autowired
    public CloudController(AssistantService assistantService, CloudStorageService cloudStorageService) {
        this.assistantService = assistantService;
        this.cloudStorageService = cloudStorageService;
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) throws InputDataException, FileUploadException, FileRewriteException {

        String username = assistantService.resolveUsername(request);
        cloudStorageService.uploadFile(username, filename, file);

        log.info("User " + username + " upload new file: " + filename);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename) throws IOException, InputDataException, FileDownloadException {

        String username = assistantService.resolveUsername(request);
        MultipartFile multipartFile = cloudStorageService.downloadFile(username, filename);

        log.info("User " + username + " download file " + filename);
        return assistantService.sendFile(multipartFile);
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename) throws InputDataException, FileNotFoundException, FileDeleteException {

        String username = assistantService.resolveUsername(request);
        cloudStorageService.deleteFile(username, filename);

        log.info("User " + username + " delete file " + filename);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @Valid @RequestBody RenameFileRequestDto newFilename) throws InputDataException, FileNotFoundException, FileRewriteException {

        String username = assistantService.resolveUsername(request);
        cloudStorageService.renameFile(username, filename, newFilename.getFilename());

        log.info("User " + username + " rename file from " + filename + " to " + newFilename.getFilename());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileFromListResponseDto>> getList(
            HttpServletRequest request,
            @RequestParam("limit") int limit) {

        String username = assistantService.resolveUsername(request);
        List<FileFromListResponseDto> filesFromList = cloudStorageService.getLimitFileList(username, limit);

        return ResponseEntity.ok(filesFromList);
    }
}
