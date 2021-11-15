package ru.netology.diploma;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.GenericContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CloudStorageApplicationTests {
    private static final int PORT = 8081;

    @Autowired
    TestRestTemplate restTemplate;

    private static GenericContainer<?> cloudApp = new GenericContainer<>("cloudapp")
            .withExposedPorts(PORT);

    @BeforeAll
    public static void setUp() {
        cloudApp.start();
    }


    @Test
    public void login_response200() throws Exception {
        ResponseEntity<String> result = restTemplate.withBasicAuth("spring", "secret")
                .getForEntity("/login", String.class);
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

}
