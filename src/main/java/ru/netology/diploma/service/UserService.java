package ru.netology.diploma.service;

import ru.netology.diploma.dao.User;


public interface UserService {
    User findUserByUsername(String username);
}
