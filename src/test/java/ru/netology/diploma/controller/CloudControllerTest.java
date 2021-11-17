package ru.netology.diploma.controller;

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
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.testcontainers.shaded.com.google.common.io.ByteStreams;
import ru.netology.diploma.dto.request.RenameFileRequestDto;
import ru.netology.diploma.dto.response.FileFromListResponseDto;
import ru.netology.diploma.pojo.exceptions.*;
import ru.netology.diploma.service.AssistantService;
import ru.netology.diploma.service.CloudStorageService;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class CloudControllerTest {

    @InjectMocks
    CloudController cloudController;

    @Mock
    AssistantService assistantService;

    @Mock
    CloudStorageService cloudStorageService;


    @Test
    void uploadFile_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        when(assistantService.resolveUsername(request)).thenReturn(username);

        ResponseEntity<?> responseEntity = assertDoesNotThrow(() -> cloudController.uploadFile(request, filename, file));
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Mockito.verify(assistantService, times(1)).resolveUsername(request);
        assertDoesNotThrow(() -> Mockito.verify(cloudStorageService, times(1)).uploadFile(username, filename, file));
    }

    @Test
    void uploadFile_throwInputDataException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new InputDataException("Incorrect file input data")).when(cloudStorageService).uploadFile(username, filename, file);
        } catch (FileRewriteException | FileUploadException e) {
            fail();
        }

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudController.uploadFile(request, filename, file));

        String message = e.getMessage();
        assertTrue(message.equals("Incorrect file input data"));
    }

    @Test
    void uploadFile_throwOtherExceptions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new FileUploadException("upload exception")).when(cloudStorageService).uploadFile(username, filename, file);
        } catch (FileUploadException | FileRewriteException e) {
            fail();
        }
        assertThrows(FileUploadException.class, () ->
                cloudController.uploadFile(request, filename, file));

        try {
            doThrow(new FileRewriteException("rewrite exception")).when(cloudStorageService).uploadFile(username, filename, file);
        } catch (FileUploadException | FileRewriteException e) {
            fail();
        }
        assertThrows(FileRewriteException.class, () ->
                cloudController.uploadFile(request, filename, file));
    }

    @Test
    void downloadFile_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String text = "Text to be uploaded.";
        MockMultipartFile file = new MockMultipartFile(filename, filename, "text/plain", text.getBytes());

        when(assistantService.resolveUsername(request)).thenReturn(username);
        assertDoesNotThrow(() -> when(cloudStorageService.downloadFile(username, filename)).thenReturn(file));

        // expected response
        try {
            byte[] body = file.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getOriginalFilename());
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(body));

            ResponseEntity<Resource> expected = ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.getSize())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(resource);

            when(assistantService.sendFile(file)).thenReturn(expected);
        } catch (IOException e) {
            fail();
        }
        //==============

        ResponseEntity<Resource> responseEntity = assertDoesNotThrow(() -> cloudController.downloadFile(request, filename));
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getHeaders().getContentType()).isEqualTo(MediaType.MULTIPART_FORM_DATA);
        assertThat(responseEntity.getHeaders().getContentLength()).isEqualTo(file.getSize());

        assertDoesNotThrow(() -> assertThat(ByteStreams.toByteArray(responseEntity.getBody().getInputStream())).isEqualTo((file.getBytes())));
    }

    @Test
    void downloadFile_throwInputDataException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new InputDataException("Incorrect file input data")).when(cloudStorageService).downloadFile(username, filename);
        } catch (FileNotFoundException | FileDownloadException e) {
            fail();
        }
        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudController.downloadFile(request, filename));

        String message = e.getMessage();
        assertTrue(message.equals("Incorrect file input data"));
    }

    @Test
    void downloadFile_throwOtherExceptions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new FileDownloadException("download exception")).when(cloudStorageService).downloadFile(username, filename);
        } catch (FileDownloadException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileDownloadException.class, () ->
                cloudController.downloadFile(request, filename));

        try {
            doThrow(new FileNotFoundException("file not found exception")).when(cloudStorageService).downloadFile(username, filename);
        } catch (FileDownloadException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileNotFoundException.class, () ->
                cloudController.downloadFile(request, filename));
    }

    @Test
    void deleteFile_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";

        when(assistantService.resolveUsername(request)).thenReturn(username);

        ResponseEntity<?> responseEntity = assertDoesNotThrow(() -> cloudController.deleteFile(request, filename));
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void deleteFile_throwInputDataException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new InputDataException("Incorrect file input data")).when(cloudStorageService).deleteFile(username, filename);
        } catch (FileNotFoundException | FileDeleteException e) {
            fail();
        }

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudController.deleteFile(request, filename));

        String message = e.getMessage();
        assertTrue(message.equals("Incorrect file input data"));
    }

    @Test
    void deleteFile_throwOtherExceptions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new FileDeleteException("delete exception")).when(cloudStorageService).deleteFile(username, filename);
        } catch (FileDeleteException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileDeleteException.class, () ->
                cloudController.deleteFile(request, filename));

        try {
            doThrow(new FileNotFoundException("file not found exception")).when(cloudStorageService).deleteFile(username, filename);
        } catch (FileDeleteException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileNotFoundException.class, () ->
                cloudController.deleteFile(request, filename));
    }

    @Test
    void renameFile_ok() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        RenameFileRequestDto renameFileRequestDto = new RenameFileRequestDto(newFilename);

        when(assistantService.resolveUsername(request)).thenReturn(username);

        ResponseEntity<?> responseEntity = assertDoesNotThrow(() -> cloudController.renameFile(request, filename, renameFileRequestDto));
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    void renameFile_throwInputDataException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        RenameFileRequestDto renameFileRequestDto = new RenameFileRequestDto(newFilename);

        when(assistantService.resolveUsername(request)).thenReturn(username);
        try {
            doThrow(new InputDataException("Incorrect file input data")).when(cloudStorageService).renameFile(username, filename, newFilename);
        } catch (FileNotFoundException | FileRewriteException e) {
            fail();
        }

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudController.renameFile(request, filename, renameFileRequestDto));

        String message = e.getMessage();
        assertTrue(message.equals("Incorrect file input data"));
    }

    @Test
    void renameFile_throwOtherExceptions() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String username = "username";
        String filename = "test.txt";
        String newFilename = "test-new-filename.txt";

        RenameFileRequestDto renameFileRequestDto = new RenameFileRequestDto(newFilename);

        when(assistantService.resolveUsername(request)).thenReturn(username);

        try {
            doThrow(new FileRewriteException("file rewrite exception")).when(cloudStorageService).renameFile(username, filename, newFilename);
        } catch (FileRewriteException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileRewriteException.class, () ->
                cloudController.renameFile(request, filename, renameFileRequestDto));

        try {
            doThrow(new FileNotFoundException("file not found exception")).when(cloudStorageService).renameFile(username, filename, newFilename);
        } catch (FileRewriteException | FileNotFoundException e) {
            System.out.println(e.getMessage());
            fail();
        }
        assertThrows(FileNotFoundException.class, () ->
                cloudController.renameFile(request, filename, renameFileRequestDto));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void getList(int limit) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        FileFromListResponseDto file1 = new FileFromListResponseDto("filename1", 1234);
        FileFromListResponseDto file2 = new FileFromListResponseDto("filename2", 2345);
        FileFromListResponseDto file3 = new FileFromListResponseDto("filename3", 3456);
        FileFromListResponseDto file4 = new FileFromListResponseDto("filename4", 4567);
        FileFromListResponseDto file5 = new FileFromListResponseDto("filename5", 5678);

        List<FileFromListResponseDto> fileList = Arrays.asList(file1, file2, file3, file4, file5);

        String username = "username";

        List<FileFromListResponseDto> limitList = fileList.stream()
                .limit(limit)
                .collect(Collectors.toList());

        when(assistantService.resolveUsername(request)).thenReturn(username);
        when(cloudStorageService.getLimitFileList(username, limit)).thenReturn(limitList);

        ResponseEntity<List<FileFromListResponseDto>> responseEntity = cloudController.getList(request, limit);
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);

        if (limit == 0) {
            assertThat(responseEntity.getBody()).hasSize(0);
        } else if (limit > fileList.size()) {
            assertThat(responseEntity.getBody()).hasSize(fileList.size());
            for (int ii = 0; ii < fileList.size(); ii++) {
                assertThat(fileList.get(ii).getFilename()).isEqualTo(responseEntity.getBody().get(ii).getFilename());
                assertThat(fileList.get(ii).getSize()).isEqualTo(responseEntity.getBody().get(ii).getSize());
            }
        } else {
            assertThat(responseEntity.getBody()).hasSize(limit);
            for (int ii = 0; ii < limit; ii++) {
                assertThat(fileList.get(ii).getFilename()).isEqualTo(responseEntity.getBody().get(ii).getFilename());
                assertThat(fileList.get(ii).getSize()).isEqualTo(responseEntity.getBody().get(ii).getSize());
            }
        }

    }
}