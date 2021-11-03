package ru.netology.diploma.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.repository.FileRepository;
import ru.netology.diploma.repository.UserRepository;
import ru.netology.diploma.service.FileService;
import ru.netology.diploma.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService, FileService {
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, FileRepository fileRepository) {//, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileRepository = fileRepository;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> list = userRepository.findAll();
        return list;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username " + username + " not found"));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findUserById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not found"));
    }

    @Override
    public void changeUserStatus(User user, Status status) {
        User changedUser = user;
        changedUser.setStatus(status);
        userRepository.save(changedUser);
    }

    @Override
    public List<StorageFile> getAllFilesByUser(Long userId) {
        User user = userRepository.findUserById(userId)
                .orElse(null);
        if (user != null)
            return fileRepository.findAllByUserAndStatusOrderByCreated(user, Status.ACTIVE);

        return new ArrayList<>();
    }

    @Override
    public void deleteFileByUserAndFilename(Long userId, String filename) {
        User user = userRepository.findUserById(userId)
                .orElse(null);

        if (user != null)
            fileRepository.deleteStorageFileByUserAndFilename(user, filename);
    }

    @Override
    public void addFile(StorageFile file) {
        fileRepository.save(file);
    }

    @Override
    public void uploadFileName(StorageFile file, String filename) {
        StorageFile fileFromStorage = file;
        fileFromStorage.setFilename(filename);
        fileRepository.save(fileFromStorage);
    }
}
