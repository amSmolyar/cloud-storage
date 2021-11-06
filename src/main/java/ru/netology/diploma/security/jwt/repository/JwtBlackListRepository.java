package ru.netology.diploma.security.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma.security.jwt.dao.JwtBlackList;

public interface JwtBlackListRepository extends JpaRepository<JwtBlackList, Long> {
    JwtBlackList findByTokenEquals(String token);

    @Override
    JwtBlackList save(JwtBlackList entity);
}
