package ru.netology.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;

import java.util.List;

public interface FileRepository extends JpaRepository<StorageFile, Long> {
    List<StorageFile> findAllByUserAndStatusOrderByCreated(User user, Status status);
    void deleteStorageFileByUserAndFilename(User user, String filename);
}
