package ru.netology.diploma.security.jwt.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.netology.diploma.security.jwt.dao.JwtBlackList;
import ru.netology.diploma.security.jwt.repository.JwtBlackListRepository;
import ru.netology.diploma.security.jwt.service.JwtBlackListService;

@Service
public class JwtBlackListServiceImpl implements JwtBlackListService {

    private final JwtBlackListRepository jwtBlackListRepository;

    @Autowired
    public JwtBlackListServiceImpl(JwtBlackListRepository jwtBlackListRepository) {
        this.jwtBlackListRepository = jwtBlackListRepository;
    }

    @Override
    public JwtBlackList findByTokenEquals(String token) {
        return jwtBlackListRepository.findByTokenEquals(token);
    }

    @Override
    public JwtBlackList save(String token) {
        JwtBlackList jwtBlackList = new JwtBlackList();
        jwtBlackList.setToken(token);
        return jwtBlackListRepository.save(jwtBlackList);
    }
}
