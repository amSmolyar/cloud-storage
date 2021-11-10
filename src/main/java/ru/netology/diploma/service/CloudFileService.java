package ru.netology.diploma.service;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.pojo.exceptions.*;

import java.io.FileNotFoundException;
import java.io.IOException;


public interface CloudFileService {
    void uploadFile(MultipartFile file, String username, String filename) throws FileUploadException;
    MultipartFile downloadFile(String username, String filename) throws FileNotFoundException, FileDownloadException;
    void renameFile(String username, String filename, String newFilename) throws FileNotFoundException, FileRewriteException;
    void deleteFile(String username, String filename) throws FileNotFoundException, FileDeleteException;
}
