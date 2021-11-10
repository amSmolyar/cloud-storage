package ru.netology.diploma.service;

import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dao.StorageFile;

import java.util.List;

public interface CloudDBService extends UserService, FileService {
    void deleteFileByUsernameAndFilename(String username, String filename);
    void uploadFileToUserStorage(String username, String filename, MultipartFile file);
    List<StorageFile> getAllFilesByUserId(Long userId);
    List<StorageFile> getAllFilesByUsername(String username);
    List<StorageFile> getLimitFilesByUsername(String username, int limit);
    StorageFile getCurrentFile(String username, String filename);
}
