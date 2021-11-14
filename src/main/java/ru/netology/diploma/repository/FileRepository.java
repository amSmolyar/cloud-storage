package ru.netology.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<StorageFile, Long> {
    Optional<StorageFile> findFirstByUserAndFilenameAndStatus(User user, String filename, Status status);
    List<StorageFile> findAllByUserAndStatusOrderByIdDesc(User user, Status status);
}
