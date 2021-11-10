package ru.netology.diploma.security.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma.security.jwt.dao.JwtBlackList;

public interface JwtBlackListRepository extends JpaRepository<JwtBlackList, Long> {
    boolean findByTokenEquals(String token);

    JwtBlackList findByToken(String token);

    @Override
    JwtBlackList save(JwtBlackList entity);
}
