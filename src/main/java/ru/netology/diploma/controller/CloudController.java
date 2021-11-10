package ru.netology.diploma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.dto.request.RenameFileRequestDto;
import ru.netology.diploma.pojo.exceptions.*;
import ru.netology.diploma.security.jwt.JwtTokenProvider;
import ru.netology.diploma.service.CloudStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.*;
import java.util.List;

@RestController
public class CloudController {
    private final JwtTokenProvider jwtTokenProvider;
    private final CloudStorageService cloudStorageService;

    @Autowired
    public CloudController(JwtTokenProvider jwtTokenProvider, CloudStorageService cloudStorageService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.cloudStorageService = cloudStorageService;
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @RequestParam("file") MultipartFile file) throws InputDataException, FileUploadException, FileRewriteException {

        cloudStorageService.inputDataValidation(file);
        cloudStorageService.inputDataValidation(filename);

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        cloudStorageService.uploadFile(username, filename, file);

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename) throws IOException, InputDataException, FileDownloadException {

        cloudStorageService.inputDataValidation(filename);

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        MultipartFile multipartFile = cloudStorageService.downloadFile(username, filename);

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
            @RequestParam("filename") String filename) throws InputDataException, FileNotFoundException, FileDeleteException {

        cloudStorageService.inputDataValidation(filename);

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        cloudStorageService.deleteFile(username, filename);


        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(
            HttpServletRequest request,
            @RequestParam("filename") String filename,
            @Valid @RequestBody RenameFileRequestDto newFilename) throws InputDataException, FileNotFoundException, FileRewriteException {

        cloudStorageService.inputDataValidation(filename);

        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        cloudStorageService.renameFile(username, filename, newFilename.getFilename());

        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<FileFromListResponseDto>> getList(
            HttpServletRequest request,
            @RequestParam("limit") int limit) {
        String username = jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(request));
        List<FileFromListResponseDto> filesFromList = cloudStorageService.getLimitFileList(username, limit);

        return ResponseEntity.ok(filesFromList);
    }
}
