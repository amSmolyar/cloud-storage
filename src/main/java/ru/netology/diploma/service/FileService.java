package ru.netology.diploma.service;

import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;

import java.util.List;

public interface FileService {
    List<StorageFile> getAllFilesByUser(Long userId);
    void deleteFileByUserAndFilename(Long userId, String filename);
    void addFile(StorageFile file);
    void uploadFileName(StorageFile file, String filename);
}
