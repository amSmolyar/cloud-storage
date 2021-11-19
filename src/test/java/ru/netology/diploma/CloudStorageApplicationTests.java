package ru.netology.diploma;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.diploma.dto.request.AuthenticationRequestDto;
import ru.netology.diploma.dto.request.RenameFileRequestDto;
import ru.netology.diploma.dto.response.AuthenticationResponseDto;
import ru.netology.diploma.dto.response.FileFromListResponseDto;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CloudStorageApplicationTests {
    private static final int PORT_APP = 8081;
    private static final int PORT_DB = 3306;

    @Autowired
    TestRestTemplate restTemplate;

    @Container
    public static DockerComposeContainer composeContainer =
            new DockerComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("db_1", PORT_DB)
                    .withExposedService("mycloudapp_1", PORT_APP);

    private String host;
    private int port;

    @BeforeEach
    void setup() {
        composeContainer.start();
        host = composeContainer.getServiceHost("mycloudapp_1", PORT_APP);
        port = composeContainer.getServicePort("mycloudapp_1", PORT_APP);
    }

    @Test
    @Transactional
    public void test_fullCycle_response200() {
        // Тест представляет из себя имитацию работы с приложением, с использованием всех его возможностей.
        // Этапы тестирования:
        //
        // 1. login
        // 2. Вывод списка файлов с limit = 5
        // 3. Загрузка файла test.txt
        // 4. Вывод списка файлов с limit = 5
        // 5. Загрузка файла test2.txt
        // 6. Вывод списка файлов с limit = 5
        // 7. Переименование файла test.txt в new_filename_test.txt
        // 8. Вывод списка файлов с limit = 5
        // 9. Вывод списка файлов с limit = 1
        // 10. Качаем файл new_filename_test.txt
        // 11. Качаем файл test2.txt
        // 12. Удаление файла new_filename_test.txt
        // 13. Вывод списка файлов с limit = 5
        // 14. Удаление файла test2.txt
        // 15. Вывод списка файлов с limit = 5
        // 16. logout



        // Логинимся, получаем токен (пользователя берем их таблицы users): ===========================================

        String username = "evgenius@gmail.com";
        String password = "password1";

        ResponseEntity<AuthenticationResponseDto> postLoginEntity = loginResponseEntity(username, password);
        assertThat(postLoginEntity.getStatusCodeValue()).isEqualTo(200);

        // Получаем токен:
        String token = "Bearer_" + Objects.requireNonNull(postLoginEntity.getBody()).getToken();


        // получаем список из limitList файлов пользователя (сейчас он должен быть пустым): ===========================================


        int limitList = 5;
        ResponseEntity<List<FileFromListResponseDto>> getListEntity = getFileListResponseEntity(token, limitList);
        List<FileFromListResponseDto> fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(0);


        // загружаем первый файл (test.txt): ===========================================



        String filename1 = "test.txt";
        String text1 = "Text to be uploaded.";
        MockMultipartFile multipartFile1 = new MockMultipartFile(filename1, filename1, "text/plain", text1.getBytes());

        ResponseEntity<String> postFileEntity = uploadFileResponseEntity(token, filename1, text1);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);



        // получаем список из limitList файлов пользователя (сейчас он должен состоять из одного файла test.txt): ===========================================



        limitList = 5;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(1);
        assertThat(fileList.get(0).getFilename()).isEqualTo(multipartFile1.getName());
        assertThat(fileList.get(0).getSize()).isEqualTo(multipartFile1.getSize());



        // загружаем второй файл (test.txt): ===========================================



        String filename2 = "test2.txt";
        String text2 = "Text to be uploaded.";
        MockMultipartFile multipartFile2 = new MockMultipartFile(filename2, filename2, "text/plain", text2.getBytes());

        postFileEntity = uploadFileResponseEntity(token, filename2, text2);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);



        // получаем список из limitList файлов пользователя (сейчас он должен состоять из двух файлов: test.txt, test2.txt): ===========================================



        limitList = 5;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(2);
        assertThat(fileList.get(0).getFilename()).isEqualTo(multipartFile2.getName());
        assertThat(fileList.get(0).getSize()).isEqualTo(multipartFile2.getSize());
        assertThat(fileList.get(1).getFilename()).isEqualTo(multipartFile1.getName());
        assertThat(fileList.get(1).getSize()).isEqualTo(multipartFile1.getSize());



        // Переименовываем первый файл (filename1):  ===========================================



        String newFilename = "new_filename_test.txt";
        ResponseEntity<String> putFileEntity = renameFileResponseEntity(token, filename1, newFilename);
        assertThat(putFileEntity.getStatusCodeValue()).isEqualTo(200);



        // получаем список из limitList файлов пользователя после переименования (сейчас он должен состоять из двух файлов: new_filename_test.txt, test2.txt): ===========================================



        limitList = 5;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(2);
        assertThat(fileList.get(0).getFilename()).isEqualTo(multipartFile2.getName());
        assertThat(fileList.get(0).getSize()).isEqualTo(multipartFile2.getSize());
        assertThat(fileList.get(1).getFilename()).isEqualTo(newFilename);
        assertThat(fileList.get(1).getSize()).isEqualTo(multipartFile1.getSize());



        // получаем список из limitList файлов пользователя после переименования (limitlist < количества файлов в хранилище. Должны быть показаны более новые): ===========================================



        limitList = 1;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(1);
        assertThat(fileList.get(0).getFilename()).isEqualTo(multipartFile2.getName());
        assertThat(fileList.get(0).getSize()).isEqualTo(multipartFile2.getSize());



        // Скачиваем первый загруженный файл (переименованный): ===========================================



        String downloadFilename = newFilename;
        ResponseEntity<byte[]> getFileEntity = downloadFileResponseEntity(token, downloadFilename);

        assertThat(getFileEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(getFileEntity.getBody()).isEqualTo(text1.getBytes());


        // Скачиваем второй загруженный файл (filename2): ===========================================



        downloadFilename = filename2;
        getFileEntity = downloadFileResponseEntity(token, downloadFilename);

        assertThat(getFileEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(getFileEntity.getBody()).isEqualTo(text2.getBytes());


        // Удаляем первый загруженный файл (переименованный): ===========================================



        String deleteFilename = newFilename;
        ResponseEntity<String> deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);

        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);



        // получаем список из limitList файлов пользователя (сейчас он должен состоять из одного файла: test2.txt): ===========================================



        limitList = 5;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(1);
        assertThat(fileList.get(0).getFilename()).isEqualTo(multipartFile2.getName());
        assertThat(fileList.get(0).getSize()).isEqualTo(multipartFile2.getSize());



        // Удаляем второй загруженный файл (test2.txt): ===========================================



        deleteFilename = filename2;
        deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);

        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);



        // получаем список из limitList файлов пользователя (сейчас он должен быть пустым): ===========================================



        limitList = 5;
        getListEntity = getFileListResponseEntity(token, limitList);
        fileList = getListEntity.getBody();

        assertThat(getListEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(fileList).hasSize(0);



        // Выходим из приложения (логаут): ==============================================


        ResponseEntity<String> postLogoutEntity = logoutResponseEntity(token);
        assertThat(postLogoutEntity.getStatusCodeValue()).isEqualTo(200);

    }


    @Test
    @Transactional
    public void test_loginErrors() {

            // Пароль с ошибкой:

        String username = "evgenius@gmail.com";
        String password = "123456";

        ResponseEntity<AuthenticationResponseDto> responseEntity = loginResponseEntity(username, password);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(401);

            // Логин с ошибкой:

        username = "evgenius";
        password = "password1";

        responseEntity = loginResponseEntity(username, password);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(401);
    }

    @Test
    @Transactional
    public void test_fileUploadErrors() {
            // login - получаем токен
        String username = "evgenius@gmail.com";
        String password = "password1";

        String token = login(username, password);

            // Успешная загрузка файла test.txt
        String filename = "test.txt";
        String text1 = "Text to be uploaded.";

        ResponseEntity<String> postFileEntity = uploadFileResponseEntity(token, filename, text1);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);

            // Попытка загрузить другой файл с тем же именем:
        String text2 = "Another text to be uploaded.";
        postFileEntity = uploadFileResponseEntity(token, filename, text2);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка загрузить файл с неподходящим именем:
        filename = "123";
        postFileEntity = uploadFileResponseEntity(token, filename, text1);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка загрузить пустой файл:
        filename = "empty.txt";
        String text3 = new String();
        postFileEntity = uploadFileResponseEntity(token, filename, text3);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Удаление загруженного файла:
        String deleteFilename = "test.txt";
        ResponseEntity<String> deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);

            // logout
        ResponseEntity<String> logoutResponseEntity = logoutResponseEntity(token);
        assertThat(logoutResponseEntity.getStatusCodeValue()).isEqualTo(200);
    }


    @Test
    @Transactional
    public void test_fileDownloadErrors() {
            // login - получаем токен
        String username = "evgenius@gmail.com";
        String password = "password1";

        String token = login(username, password);

            // Успешная загрузка файла test.txt
        String filename = "test.txt";
        String text = "Text to be uploaded.";

        ResponseEntity<String> postFileEntity = uploadFileResponseEntity(token, filename, text);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);

            // Попытка скачать не существующий файл:
        filename = "somefile.jpg";
        ResponseEntity<byte[]> getFileEntity = downloadFileResponseEntity(token, filename);
        assertThat(getFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка скачать файл с неподходящим именем:
        String errorFilename = "123";
        getFileEntity = downloadFileResponseEntity(token, errorFilename);
        assertThat(getFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Удаление загруженного файла:
        String deleteFilename = "test.txt";
        ResponseEntity<String> deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);

            // logout
        ResponseEntity<String> logoutResponseEntity = logoutResponseEntity(token);
        assertThat(logoutResponseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @Transactional
    public void test_fileRenameErrors() {
            // login - получаем токен
        String username = "evgenius@gmail.com";
        String password = "password1";

        String token = login(username, password);

            // Успешная загрузка файла test.txt
        String filename = "test.txt";
        String text = "Text to be uploaded.";

        ResponseEntity<String> postFileEntity = uploadFileResponseEntity(token, filename, text);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);

            // Попытка переименовать не существующий файл:
        filename = "somefile.jpg";
        ResponseEntity<String> putFileEntity = renameFileResponseEntity(token, filename, "newfilename.jpg");
        assertThat(putFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка переименовать файл с неподходящим именем:
        String errorFilename = "123";
        putFileEntity = renameFileResponseEntity(token, errorFilename, "newfilename.jpg");
        assertThat(putFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка присвоить файлу не подходящее имя:
        filename = "test.txt";
        String errorNewFilename = "123";
        putFileEntity = renameFileResponseEntity(token, filename, errorNewFilename);
        assertThat(putFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка присвоить файлу имя, которое уже занято:
        filename = "test.txt";
        putFileEntity = renameFileResponseEntity(token, filename, filename);
        assertThat(putFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Удаление загруженного файла:
        String deleteFilename = "test.txt";
        ResponseEntity<String> deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);

            // logout
        ResponseEntity<String> logoutResponseEntity = logoutResponseEntity(token);
        assertThat(logoutResponseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @Transactional
    public void test_fileDeleteErrors() {
            // login - получаем токен
        String username = "evgenius@gmail.com";
        String password = "password1";

        String token = login(username, password);

            // Успешная загрузка файла test.txt
        String filename = "test.txt";
        String text = "Text to be uploaded.";

        ResponseEntity<String> postFileEntity = uploadFileResponseEntity(token, filename, text);
        assertThat(postFileEntity.getStatusCodeValue()).isEqualTo(200);

            // Попытка удалить не существующий файл:
        filename = "somefile.jpg";
        ResponseEntity<String> deleteFileEntity = deleteFileResponseEntity(token, filename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Попытка удалить файл с неподходящим именем:
        String errorFilename = "123";
        deleteFileEntity = deleteFileResponseEntity(token, errorFilename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(400);

            // Удаление загруженного файла:
        String deleteFilename = "test.txt";
        deleteFileEntity = deleteFileResponseEntity(token, deleteFilename);
        assertThat(deleteFileEntity.getStatusCodeValue()).isEqualTo(200);

            // logout
        ResponseEntity<String> logoutResponseEntity = logoutResponseEntity(token);
        assertThat(logoutResponseEntity.getStatusCodeValue()).isEqualTo(200);
    }


    public ResponseEntity<AuthenticationResponseDto> loginResponseEntity(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        AuthenticationRequestDto authenticationRequestDto = new AuthenticationRequestDto(username, password);
        HttpEntity<AuthenticationRequestDto> requestEntity = new HttpEntity<>(authenticationRequestDto, headers);

        String url = "http://" + host + ":" + port + "/login";
        return restTemplate.postForEntity(url, requestEntity, AuthenticationResponseDto.class);
    }

    public String login(String username, String password) {
        AuthenticationResponseDto authenticationResponseDto = loginResponseEntity(username, password).getBody();
        assert authenticationResponseDto != null;
        return  "Bearer_" + authenticationResponseDto.getToken();
    }

    public ResponseEntity<List<FileFromListResponseDto>> getFileListResponseEntity(String token, int limit) {
        String url = "http://" + host + ":" + port + "/list";
        String urlTemplateList = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("limit", limit)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", token);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(urlTemplateList,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<FileFromListResponseDto>>() {});
    }

    public ResponseEntity<String> uploadFileResponseEntity(String token, String filename, String text) {
        String url = "http://" + host + ":" + port + "/file";
        String urlTemplateFile = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("filename", filename)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.add("auth-token", token);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename(filename)
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());

        HttpEntity<byte[]> fileEntity = new HttpEntity<>(text.getBytes(), fileMap);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        return restTemplate.exchange(urlTemplateFile,
                HttpMethod.POST,
                requestEntity,
                String.class);
    }

    public ResponseEntity<String> renameFileResponseEntity(String token, String filename, String newFilename) {
        String url = "http://" + host + ":" + port + "/file";
        String urlTemplateFile = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("filename", filename)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", token);

        RenameFileRequestDto renameFileRequestDto = new RenameFileRequestDto(newFilename);
        HttpEntity<RenameFileRequestDto> requestEntity = new HttpEntity<>(renameFileRequestDto, headers);

        return restTemplate.exchange(urlTemplateFile,
                HttpMethod.PUT,
                requestEntity,
                String.class);
    }

    public ResponseEntity<byte[]> downloadFileResponseEntity(String token, String filename) {
        String url = "http://" + host + ":" + port + "/file";
        String urlTemplateFile = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("filename", filename)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add("auth-token", token);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(urlTemplateFile,
                HttpMethod.GET,
                requestEntity,
                byte[].class);
    }


    public ResponseEntity<String> deleteFileResponseEntity(String token, String filename) {
        String url = "http://" + host + ":" + port + "/file";
        String urlTemplateFile = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("filename", filename)
                .encode()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", token);

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(urlTemplateFile,
                HttpMethod.DELETE,
                requestEntity,
                String.class);
    }

    public ResponseEntity<String> logoutResponseEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("auth-token", token);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        String url = "http://" + host + ":" + port + "/logout";
        return restTemplate.postForEntity(url, requestEntity, String.class);
    }
}
