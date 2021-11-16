package ru.netology.diploma.service.impl;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.pojo.exceptions.FileDeleteException;
import ru.netology.diploma.pojo.exceptions.FileRewriteException;
import ru.netology.diploma.pojo.exceptions.InputDataException;
import ru.netology.diploma.service.CloudDBService;
import ru.netology.diploma.service.CloudFileService;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class CloudStorageServiceImplTest {
    @InjectMocks
    CloudStorageServiceImpl cloudStorageService;

    @Mock
    CloudDBService cloudDBService;

    @Mock
    CloudFileService cloudFileService;


    @Test
    void inputDataValidation_throwInputDataException() {
        String filename = "test.txt";
        String text = "Text to be uploaded.";

        assertThrows(InputDataException.class, () ->
                cloudStorageService.inputDataValidation(new MockMultipartFile(filename, "", "text/plain", text.getBytes())));

        assertThrows(InputDataException.class, () ->
                cloudStorageService.inputDataValidation(""));

        assertThrows(InputDataException.class, () ->
                cloudStorageService.inputDataValidation("filename"));

        assertThrows(InputDataException.class, () ->
                cloudStorageService.inputDataValidation(new String()));
    }

    @Test
    void uploadFile_withoutException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(null);

        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        assertDoesNotThrow(() -> cloudStorageService.uploadFile(username, filename, file));

        Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
        assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).uploadFile(file, username, filename));
        Mockito.verify(cloudDBService, times(1)).uploadFileToUserStorage(username, filename, file);

    }

    @Test
    void uploadFile_throwFileRewriteException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        FileRewriteException e = assertThrows(FileRewriteException.class, () ->
                cloudStorageService.uploadFile(username, filename, file));

        String message = e.getMessage();
        assertTrue(message.equals("File with name " + filename + " already exists"));
    }

    @Test
    void downloadFile_withoutException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        assertDoesNotThrow(() -> cloudStorageService.downloadFile(username, filename));

        Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
        assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).downloadFile(username, filename));
    }

    @Test
    void downloadFile_throwFileNotFoundException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(null);

        FileNotFoundException e = assertThrows(FileNotFoundException.class, () ->
                cloudStorageService.downloadFile(username, filename));

        String message = e.getMessage();
        assertTrue(message.equals("File with name " + filename + " does not exist in your storage"));
    }

    @Test
    void renameFile_withoutException() {
        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        assertDoesNotThrow(() -> cloudStorageService.renameFile(username, filename, newFilename));

        Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
        assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).renameFile(username, filename, newFilename));
        Mockito.verify(cloudDBService, times(1)).renameFile(username, filename, newFilename);
    }

    @Test
    void renameFile_throwsInputDataException() {
        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(null);

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudStorageService.renameFile(username, filename, newFilename));

        String message = e.getMessage();
        assertTrue(message.equals("File with name " + filename + " does not exist in storage"));
    }

    @Test
    void renameFile_throwsFileNotFoundException() throws FileNotFoundException, FileRewriteException {
        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        doThrow(new FileNotFoundException()).when(cloudFileService).renameFile(username, filename, newFilename);

        try {
            cloudStorageService.renameFile(username, filename, newFilename);
        } catch (FileNotFoundException e) {
            Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
            assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).renameFile(username, filename, newFilename));
            Mockito.verify(cloudDBService, times(1)).deleteFileByUsernameAndFilename(username, filename);
        }
    }

    @Test
    void renameFile_throwsFileRewriteException() throws FileNotFoundException, FileRewriteException {
        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        doThrow(new FileRewriteException("rewrite exception")).when(cloudFileService).renameFile(username, filename, newFilename);

        try {
            cloudStorageService.renameFile(username, filename, newFilename);
        } catch (FileRewriteException e) {
            Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
            assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).renameFile(username, filename, newFilename));
        }
    }

    @Test
    void deleteFile_withoutException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        assertDoesNotThrow(() -> cloudStorageService.deleteFile(username, filename));

        Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
        assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).deleteFile(username, filename));
        Mockito.verify(cloudDBService, times(1)).deleteFileByUsernameAndFilename(username, filename);
    }

    @Test
    void deleteFile_throwsInputDataException() {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(null);

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudStorageService.deleteFile(username, filename));

        String message = e.getMessage();
        assertTrue(message.equals("File with name " + filename + " does not exist in storage"));
    }

    @Test
    void deleteFile_throwsFileNotFoundException() throws FileNotFoundException, FileDeleteException {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        doThrow(new FileNotFoundException()).when(cloudFileService).deleteFile(username, filename);

        try {
            cloudStorageService.deleteFile(username, filename);
        } catch (FileNotFoundException e) {
            Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
            assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).deleteFile(username, filename));
            Mockito.verify(cloudDBService, times(1)).deleteFileByUsernameAndFilename(username, filename);
        }
    }

    @Test
    void deleteFile_throwsFileDeleteException() throws FileNotFoundException, FileDeleteException {
        String username = "username";
        String filename = "test.txt";

        Mockito.when(cloudDBService.getCurrentFile(username, filename))
                .thenReturn(new StorageFile());

        doThrow(new FileDeleteException("delete exception")).when(cloudFileService).deleteFile(username, filename);

        try {
            cloudStorageService.deleteFile(username, filename);
        } catch (FileDeleteException e) {
            Mockito.verify(cloudDBService, times(1)).getCurrentFile(username, filename);
            assertDoesNotThrow(() -> Mockito.verify(cloudFileService, times(1)).deleteFile(username, filename));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void getLimitFileList(int limit) {
        List<StorageFile> fileList = new ArrayList<>();

        // creating user
        User user = new User();
        user.setUsername("user1@gmail.com");
        user.setFirstName("firstname1");
        user.setLastName("lastname1");
        user.setPassword("password1");
        user.setUserFiles(new ArrayList<>());
        user.setId(1L);
        user.setCreated(new Date());
        user.setUpdated(new Date());
        user.setStatus(Status.ACTIVE);

        // creating file1
        StorageFile file1 = new StorageFile();
        file1.setFilename("filename1");
        file1.setFileSize(1234);
        file1.setCreated(new Date());
        file1.setUpdated(new Date());
        file1.setStatus(Status.ACTIVE);
        file1.setUser(user);
        file1.setId(1L);

        // creating file2
        StorageFile file2 = new StorageFile();
        file2.setFilename("filename2");
        file2.setFileSize(2345);
        file2.setCreated(new Date());
        file2.setUpdated(new Date());
        file2.setStatus(Status.ACTIVE);
        file2.setUser(user);
        file2.setId(2L);

        // creating file3
        StorageFile file3 = new StorageFile();
        file3.setFilename("filename3");
        file3.setFileSize(3456);
        file3.setCreated(new Date());
        file3.setUpdated(new Date());
        file3.setStatus(Status.DELETED);
        file3.setUser(user);
        file3.setId(3L);

        // creating file4
        StorageFile file4 = new StorageFile();
        file4.setFilename("filename3");
        file4.setFileSize(3456);
        file4.setCreated(new Date());
        file4.setUpdated(new Date());
        file4.setStatus(Status.ACTIVE);
        file4.setUser(user);
        file4.setId(4L);

        // creating file5
        StorageFile file5 = new StorageFile();
        file5.setFilename("filename3");
        file5.setFileSize(3456);
        file5.setCreated(new Date());
        file5.setUpdated(new Date());
        file5.setStatus(Status.ACTIVE);
        file5.setUser(user);
        file5.setId(5L);

        fileList.add(file1);
        fileList.add(file2);
        fileList.add(file3);
        fileList.add(file4);
        fileList.add(file5);

        List<StorageFile> limitList = fileList.stream()
                .limit(limit)
                .collect(Collectors.toList());

        Mockito.when(cloudDBService.getLimitFilesByUsername("anyUsername", limit))
                .thenReturn(limitList);

        List<FileFromListResponseDto> actual = cloudStorageService.getLimitFileList("anyUsername", limit);

        if (limit == 0) {
            assertThat(actual).hasSize(0);
        } else if (limit > fileList.size()) {
            assertThat(actual).hasSize(fileList.size());
            for (int ii = 0; ii < fileList.size(); ii++) {
                assertThat(fileList.get(ii).getFilename()).isEqualTo(actual.get(ii).getFilename());
                assertThat(fileList.get(ii).getFileSize()).isEqualTo(actual.get(ii).getSize());
            }
        } else {
            assertThat(actual).hasSize(limit);
            for (int ii = 0; ii < limit; ii++) {
                assertThat(fileList.get(ii).getFilename()).isEqualTo(actual.get(ii).getFilename());
                assertThat(fileList.get(ii).getFileSize()).isEqualTo(actual.get(ii).getSize());
            }
        }

    }
}