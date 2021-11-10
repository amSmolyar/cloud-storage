package ru.netology.diploma.service;

import ru.netology.diploma.dao.StorageFile;

public interface FileService {
    void addFile(StorageFile file);
    void renameFile(String username, String filename, String newFilename);
}
