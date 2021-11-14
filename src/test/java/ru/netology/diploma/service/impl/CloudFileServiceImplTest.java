package ru.netology.diploma.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.diploma.pojo.exceptions.FileDownloadException;
import ru.netology.diploma.pojo.exceptions.FileUploadException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class CloudFileServiceImplTest {

    @InjectMocks
    CloudFileServiceImpl cloudFileService;

    @Value("${storage.upload.path}")
    private String uploadPath;

    private static List<Path> filesToBeDeleted = new ArrayList<>();

    @ParameterizedTest
    @ValueSource(strings = {"username1", "username2", "username3"})
    void uploadFile_withoutException(String username) {
        String filename = "test.txt";

        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile("file", filename, "text/plain", text.getBytes());

        assertDoesNotThrow(() -> cloudFileService.uploadFile(file, username, filename));

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);
        assertTrue(Files.exists(path));

        filesToBeDeleted.add(path);
    }

    @Test
    void uploadFile_FileUploadException() {
        // username is empty
        String username = " ";
        String filename = "test.txt";

        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile("file", filename, "text/plain", text.getBytes());

        FileUploadException e = assertThrows(FileUploadException.class, () ->
                cloudFileService.uploadFile(file, username, filename));

        String message = e.getMessage();
        assertTrue(message.equals("Can't create directory. Error path"));

        // filename is empty
        String username2 = "username1";
        String filename2 = "";

        MockMultipartFile file2 = new MockMultipartFile("file", filename2, "text/plain", text.getBytes());

        FileUploadException e2 = assertThrows(FileUploadException.class, () ->
                cloudFileService.uploadFile(file2, username2, filename2));

        String message2 = e2.getMessage();
        assertTrue(message2.equals("Can't upload file " + filename2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"username1", "username2", "username3"})
    void downloadFile_withoutException(String username) {
        // upload file
        String filename = "test.txt";

        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        assertDoesNotThrow(() -> cloudFileService.uploadFile(file, username, filename));

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);
        filesToBeDeleted.add(path);

        // download file
        try {
            MultipartFile actual = cloudFileService.downloadFile(username, filename);
            assertThat(actual.getName()).isEqualTo(file.getName());
            assertThat(actual.getOriginalFilename()).isEqualTo(file.getOriginalFilename());
            assertThat(actual.getSize()).isEqualTo(file.getSize());
            assertThat(actual.getContentType()).isEqualTo(file.getContentType());
            assertThat(actual.getBytes()).isEqualTo(file.getBytes());
        } catch (FileDownloadException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Test
    void downloadFile_throwException() {
        String username = "username1";
        String filename = "test.txt";

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);

        // file not found
        FileNotFoundException e = assertThrows(FileNotFoundException.class, () ->
                cloudFileService.downloadFile(username, filename));

        String message = e.getMessage();
        assertTrue(message.equals("File " + path + " not found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"username1", "username2", "username3"})
    void renameFile(String username) {
        String newFilename = "new-name-test.txt";

        // upload file
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        assertDoesNotThrow(() -> cloudFileService.uploadFile(file, username, filename));

        // rename file
        assertDoesNotThrow(() -> cloudFileService.renameFile(username, filename, newFilename));

        Path path = Paths.get(uploadPath + "/" + username + "/" + newFilename);
        assertTrue(Files.exists(path));

        filesToBeDeleted.add(path);
    }

    @Test
    void renameFile_throwException() {
        String username = "username1";
        String filename = "test.txt";
        String newFilename = "new-name-test.txt";

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);

        // file not found
        FileNotFoundException e = assertThrows(FileNotFoundException.class, () ->
                cloudFileService.renameFile(username, filename, newFilename));

        String message = e.getMessage();
        assertTrue(message.equals("File " + path + " not found"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"username1", "username2", "username3"})
    void deleteFile(String username) {
        // upload file
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        assertDoesNotThrow(() -> cloudFileService.uploadFile(file, username, filename));

        // delete file
        assertDoesNotThrow(() -> cloudFileService.deleteFile(username, filename));

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);
        assertTrue(Files.notExists(path));
    }

    @Test
    void deleteFile_throwException() {
        String username = "username1";
        String filename = "test.txt";

        Path path = Paths.get(uploadPath + "/" + username + "/" + filename);

        // file not found
        FileNotFoundException e = assertThrows(FileNotFoundException.class, () ->
                cloudFileService.deleteFile(username, filename));

        String message = e.getMessage();
        assertTrue(message.equals("File " + path + " not found"));
    }

    @AfterEach
    public void deleteTestFiles() {
        filesToBeDeleted.forEach(path -> {
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}