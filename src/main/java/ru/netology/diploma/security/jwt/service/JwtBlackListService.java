package ru.netology.diploma.security.jwt.service;

import ru.netology.diploma.security.jwt.dao.JwtBlackList;


public interface JwtBlackListService {
    JwtBlackList findByToken(String token);
    boolean findByTokenEquals(String token);
    JwtBlackList save(String token);
}
