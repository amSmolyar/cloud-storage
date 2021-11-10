package ru.netology.diploma.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.pojo.exceptions.InputDataException;
import ru.netology.diploma.repository.FileRepository;
import ru.netology.diploma.repository.UserRepository;
import ru.netology.diploma.service.CloudDBService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CloudDBServiceImpl implements CloudDBService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Autowired
    public CloudDBServiceImpl(UserRepository userRepository, FileRepository fileRepository) {//, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new InputDataException("User with username " + username + " not found"));
    }

    @Override
    public User findUserById(Long id) {
        return userRepository.findUserById(id)
                .orElseThrow(() -> new InputDataException("User with id " + id + " not found"));
    }

    @Override
    public StorageFile getCurrentFile(String username, String filename) {
        User user = findUserByUsername(username);
        return fileRepository.findFirstByUserAndFilenameAndStatus(user, filename, Status.ACTIVE)
                .orElse(null);
    }

    @Override
    public List<StorageFile> getAllFilesByUserId(Long userId) {
        User user = userRepository.findUserById(userId)
                .orElse(null);
        if (user != null)
            return fileRepository.findAllByUserAndStatusOrderByIdDesc(user, Status.ACTIVE);

        return new ArrayList<>();
    }

    @Override
    public List<StorageFile> getAllFilesByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElse(null);
        if (user != null)
            return fileRepository.findAllByUserAndStatusOrderByIdDesc(user, Status.ACTIVE);

        return new ArrayList<>();
    }

    @Override
    public List<StorageFile> getLimitFilesByUsername(String username, int limit) {
        if (limit < 0)
            throw new InputDataException("Limit must be positive");

        return getAllFilesByUsername(username)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());

    }

    @Override
    public void uploadFileToUserStorage(String username, String filename, MultipartFile file) {
        User user = findUserByUsername(username);
        int fileSize = (int) file.getSize();
        StorageFile fileUpload = new StorageFile(filename, fileSize, user);
        fileUpload.setId(fileRepository.findMaxId() + 1);
        fileUpload.setCreated((new Date()));
        fileUpload.setUpdated((new Date()));
        fileUpload.setStatus(Status.ACTIVE);
        this.addFile(fileUpload);
    }


    @Override
    public void deleteFileByUsernameAndFilename(String username, String filename) {
        StorageFile file = getCurrentFile(username, filename);
        if (file == null)
            throw new InputDataException("File with name " + filename + " does not exist in storage");

        file.setStatus(Status.DELETED);
        file.setUpdated(new Date());
        fileRepository.save(file);
    }

    @Override
    public void renameFile(String username, String filename, String newFilename) {
        StorageFile file = getCurrentFile(username, filename);
        if (file == null)
            throw new InputDataException("File with name " + filename + " does not exist in storage");

        StorageFile checkFile = getCurrentFile(username, newFilename);
        if (checkFile != null)
            throw new InputDataException("File with name " + newFilename + " is already exists");

        file.setFilename(newFilename);
        file.setUpdated(new Date());
        fileRepository.save(file);
    }

    @Override
    public void addFile(StorageFile file) {
        fileRepository.save(file);
    }
}
