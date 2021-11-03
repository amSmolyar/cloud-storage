package ru.netology.diploma.service;

import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User findByUsername(String username);
    User findById(Long id);
    void changeUserStatus(User user, Status status);
}
