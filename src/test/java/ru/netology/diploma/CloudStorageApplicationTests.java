package ru.netology.diploma;

import liquibase.pro.packaged.A;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.dto.response.AuthenticationResponseDto;
import ru.netology.diploma.repository.FileRepository;
import ru.netology.diploma.repository.UserRepository;
import ru.netology.diploma.security.jwt.repository.JwtBlackListRepository;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestPropertySource(value = {"classpath:application.properties"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ContextConfiguration(initializers = CloudStorageApplicationTests.DockerMysqlDataSourceInitializer.class)
@Testcontainers
class CloudStorageApplicationTests {
    private static final int PORT = 8081;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private JwtBlackListRepository jwtBlackListRepository;

    @ClassRule
    public static Network mycloudnetwork = Network.newNetwork();

    @Container
    public static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withNetwork(mycloudnetwork)
            .withReuse(true);

    @Container
    private static GenericContainer<?> cloudApp = new GenericContainer<>("mycloudapp:latest")
            .withNetwork(mycloudnetwork)
            .withExposedPorts(PORT);

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        registry.add("spring.liquibase.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.liquibase.user", mySQLContainer::getUsername);
        registry.add("spring.liquibase.password", mySQLContainer::getPassword);
    }

    @BeforeAll
    public static void setUp() {
        mySQLContainer.start();
        cloudApp.start();
    }

    @Test
    void test1() {
        mySQLContainer.withClasspathResourceMapping("application.properties", "/db/changelog/001-schema.xml", BindMode.READ_ONLY);
        User user = userRepository.findUserByUsername("evgenius@gmail.com")
                .orElse(null);

        assertThat(user).isNotNull();
    }


//    @Test
//    public void all_tasks_response200() {
//        // Логинимся:
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto("evgenius@gmaol.com", "password1");
//
//        HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authenticationRequestDto, headers);
//
//        String url = "http://localhost:" + cloudApp.getMappedPort(PORT) + "/login";
//        ResponseEntity<AuthenticationResponseDto> postEntity = restTemplate.postForEntity(url, requestEntity, AuthenticationResponseDto.class);
//        AuthenticationResponseDto authenticationResponseDto = postEntity.getBody();
//        String token = "Bearer_" + authenticationResponseDto.getToken();
//
//        assertTrue(postEntity.getStatusCodeValue() == 200);
//
//        // получаем список файлов пользователя:
//        headers.add("auth-token", token);
//
//    }

}
