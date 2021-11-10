package ru.netology.diploma.service;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.pojo.exceptions.*;

import java.io.FileNotFoundException;
import java.util.List;

public interface CloudStorageService {
    void inputDataValidation(MultipartFile file) throws InputDataException;
    void inputDataValidation(String filename) throws InputDataException;

    void uploadFile(String username, String filename, MultipartFile file) throws FileRewriteException, FileUploadException;
    MultipartFile downloadFile(String username, String filename)  throws FileNotFoundException, FileDownloadException;
    void renameFile(String username, String filename, String newFilename)  throws FileNotFoundException, FileRewriteException;
    void deleteFile(String username, String filename) throws FileNotFoundException, FileDeleteException;

    List<FileFromListResponseDto> getLimitFileList(String username, int limit);
}
