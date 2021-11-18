package ru.netology.diploma;

import org.junit.ClassRule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.dto.response.AuthenticationResponseDto;


import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CloudStorageApplicationTests {
    private static final int PORT_APP = 8081;
    private static final int PORT_DB = 3307;

    @Autowired
    TestRestTemplate restTemplate;


    @ClassRule
    public static DockerComposeContainer cloudApp =
            new DockerComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("db_1", PORT_DB)
                    .withExposedService("mycloudapp_1", PORT_APP)
                    .withLocalCompose(true);


    @Test
    public void all_tasks_response200() throws Exception {
        // Логинимся:

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto("evgenius@gmaol.com", "password1");

        HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authenticationRequestDto, headers);

        String url = "http://localhost:" + (PORT_APP) + "/login";
        ResponseEntity<AuthenticationResponseDto> postEntity = restTemplate.postForEntity(url, requestEntity, AuthenticationResponseDto.class);
        AuthenticationResponseDto authenticationResponseDto = postEntity.getBody();
        String token = "Bearer_" + authenticationResponseDto.getToken();

        assertTrue(postEntity.getStatusCodeValue() == 200);

        // получаем список файлов пользователя:
        headers.add("auth-token", token);

    }

}
