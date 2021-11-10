package ru.netology.diploma.service;

import ru.netology.diploma.dao.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User findUserByUsername(String username);
    User findUserById(Long id);
}
