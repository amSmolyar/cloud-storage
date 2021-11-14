package ru.netology.diploma.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.diploma.dao.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAll();
    Optional<User> findUserByUsername(String username);
}
