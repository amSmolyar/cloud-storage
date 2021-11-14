package ru.netology.diploma.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.User;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JpaUserTest {

    @Autowired
    UserRepository userRepository;

    @Test
    public void test_findNoUsers_emptyRepository() {
        Iterable<User> users = userRepository.findAll();

        assertThat(users).isEmpty();
    }

    @Test
    public void test_save_and_findAllUsers() {
        User user1 = new User();
        user1.setUsername("user1@gmail.com");
        user1.setFirstName("firstname1");
        user1.setLastName("lastname1");
        user1.setPassword("password1");
        user1.setUserFiles(new ArrayList<>());
        user1.setId(1L);
        user1.setCreated(new Date());
        user1.setUpdated(new Date());
        user1.setStatus(Status.ACTIVE);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2@gmail.com");
        user2.setFirstName("firstname2");
        user2.setLastName("lastname2");
        user2.setPassword("password2");
        user2.setUserFiles(new ArrayList<>());
        user2.setId(2L);
        user2.setCreated(new Date());
        user2.setUpdated(new Date());
        user2.setStatus(Status.ACTIVE);
        userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("user3@gmail.com");
        user3.setFirstName("firstname3");
        user3.setLastName("lastname3");
        user3.setPassword("password3");
        user3.setUserFiles(new ArrayList<>());
        user3.setId(3L);
        user3.setCreated(new Date());
        user3.setUpdated(new Date());
        user3.setStatus(Status.ACTIVE);
        userRepository.save(user3);

        List<String> users = userRepository.findAll().stream()
                .map((x) -> x.toString())
                .collect(Collectors.toList());

        assertThat(users).hasSize(3);
        assertThat(users).contains(user1.toString(), user2.toString(), user3.toString());
    }

    @Test
    public void test_findByUsername() {
        User user1 = new User();
        user1.setUsername("user1@gmail.com");
        user1.setFirstName("firstname1");
        user1.setLastName("lastname1");
        user1.setPassword("password1");
        user1.setUserFiles(new ArrayList<>());
        user1.setId(1L);
        user1.setCreated(new Date());
        user1.setUpdated(new Date());
        user1.setStatus(Status.ACTIVE);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2@gmail.com");
        user2.setFirstName("firstname2");
        user2.setLastName("lastname2");
        user2.setPassword("password2");
        user2.setUserFiles(new ArrayList<>());
        user2.setId(2L);
        user2.setCreated(new Date());
        user2.setUpdated(new Date());
        user2.setStatus(Status.NOT_ACTIVE);
        userRepository.save(user2);

        User user3 = new User();
        user3.setUsername("user3@gmail.com");
        user3.setFirstName("firstname3");
        user3.setLastName("lastname3");
        user3.setPassword("password3");
        user3.setUserFiles(new ArrayList<>());
        user3.setId(3L);
        user3.setCreated(new Date());
        user3.setUpdated(new Date());
        user3.setStatus(Status.DELETED);
        userRepository.save(user3);

        User findUser1 = userRepository.findUserByUsername(user1.getUsername())
                .orElse(null);

        User findUser2 = userRepository.findUserByUsername(user2.getUsername())
                .orElse(null);

        User findUser3 = userRepository.findUserByUsername(user3.getUsername())
                .orElse(null);

        assertThat(findUser1.toString()).isEqualTo(user1.toString());
        assertThat(findUser1.getPassword()).isEqualTo(user1.getPassword());
        assertThat(findUser1.getCreated()).isEqualTo(user1.getCreated());
        assertThat(findUser1.getUpdated()).isEqualTo(user1.getUpdated());
        assertThat(findUser1.getStatus()).isEqualTo(user1.getStatus());

        assertThat(findUser2.toString()).isEqualTo(user2.toString());
        assertThat(findUser2.getPassword()).isEqualTo(user2.getPassword());
        assertThat(findUser2.getCreated()).isEqualTo(user2.getCreated());
        assertThat(findUser2.getUpdated()).isEqualTo(user2.getUpdated());
        assertThat(findUser2.getStatus()).isEqualTo(user2.getStatus());

        assertThat(findUser3.toString()).isEqualTo(user3.toString());
        assertThat(findUser3.getPassword()).isEqualTo(user3.getPassword());
        assertThat(findUser3.getCreated()).isEqualTo(user3.getCreated());
        assertThat(findUser3.getUpdated()).isEqualTo(user3.getUpdated());
        assertThat(findUser3.getStatus()).isEqualTo(user3.getStatus());
    }
}
