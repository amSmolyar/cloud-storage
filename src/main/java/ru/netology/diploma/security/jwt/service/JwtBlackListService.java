package ru.netology.diploma.security.jwt.service;

import ru.netology.diploma.security.jwt.dao.JwtBlackList;


public interface JwtBlackListService {
    JwtBlackList findByTokenEquals(String token);
    JwtBlackList save(String token);
}
