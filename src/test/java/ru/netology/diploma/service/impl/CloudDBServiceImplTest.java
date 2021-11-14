package ru.netology.diploma.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.stubbing.Answer;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;
import ru.netology.diploma.pojo.exceptions.InputDataException;
import ru.netology.diploma.repository.FileRepository;
import ru.netology.diploma.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.*;
import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class CloudDBServiceImplTest {

    @InjectMocks
    CloudDBServiceImpl cloudDBService;

    @Mock
    UserRepository userRepository;

    @Mock
    FileRepository fileRepository;


    private static User user1;
    private static User user2;
    private static User user3;
    private static StorageFile file1;
    private static StorageFile file2;
    private static StorageFile file3;
    private static StorageFile file4;
    private static StorageFile file5;


    public static Stream<User> getUsers() {
        uploadDataToDB();
        List<User> userList = List.of(user1, user2, user3);
        return userList.stream();
    }

    @Test
    void findUserByUsername_ok() {
        uploadDataToDB();
        List<User> userList = List.of(user1, user2, user3);

        for (int ii = 0; ii < userList.size(); ii++) {
            User user = userList.get(ii);
            String username = user.getUsername();
            Mockito.when(userRepository.findUserByUsername(username))
                    .thenReturn(Optional.of(user));

            assertDoesNotThrow(() ->
                    cloudDBService.findUserByUsername(username));
            User actual = cloudDBService.findUserByUsername(username);
            assertThat(actual.toString()).isEqualTo(user.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "username4", "anotherName"})
    void findUserByUsername_throwInputDataException(String name) {
        Answer<Optional<User>> answer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(name))
                .thenAnswer(answer);

        InputDataException e = assertThrows(InputDataException.class, () ->
                cloudDBService.findUserByUsername(name));

        String message = e.getMessage();
        assertTrue(message.equals("User with username " + name + " not found"));
    }

    @ParameterizedTest
    @MethodSource("getUsers")
    void getCurrentFile_notNull(User user) {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<Optional<StorageFile>> fileAnswer = findFileByUserAndFilenameAndStatusAnswer();
        Mockito.when(fileRepository.findFirstByUserAndFilenameAndStatus(any(User.class), anyString(), any(Status.class)))
                .thenAnswer(fileAnswer);

        List<StorageFile> userFiles = user.getUserFiles();
        for (int ii = 0; ii < userFiles.size(); ii++) {
            String filename = userFiles.get(ii).getFilename();
            StorageFile actual = cloudDBService.getCurrentFile(user.getUsername(), filename);

            if (user.getUsername().equals(user1.getUsername()) && filename.equals(file3.getFilename())) {
                assertThat(actual).isNull();
            } else {
                assertThat(actual).isNotNull();
                assertThat(actual.toString()).isEqualTo(userFiles.get(ii).toString());
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getUsers")
    void getCurrentFile_null(User user) {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<Optional<StorageFile>> fileAnswer = findFileByUserAndFilenameAndStatusAnswer();
        Mockito.when(fileRepository.findFirstByUserAndFilenameAndStatus(any(User.class), anyString(), any(Status.class)))
                .thenAnswer(fileAnswer);

        List<String> fileNames = List.of("1", "filename4", "filename5");
        for (int ii = 0; ii < fileNames.size(); ii++) {
            String filename = fileNames.get(ii);
            StorageFile actual = cloudDBService.getCurrentFile(user.getUsername(), filename);
            assertThat(actual).isNull();
        }
    }

    @ParameterizedTest
    @MethodSource("getUsers")
    void getAllFilesByUsername(User user) {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<List<StorageFile>> fileAnswer = findAllByUserAndStatusOrderByIdDescAnswer();
        Mockito.when(fileRepository.findAllByUserAndStatusOrderByIdDesc(any(User.class), any(Status.class)))
                .thenAnswer(fileAnswer);

        List<StorageFile> actual = cloudDBService.getAllFilesByUsername(user.getUsername());

        if (user.getUsername().equals(user1.getUsername())) {
            assertThat(actual).hasSize(2);
            assertThat(actual.get(0).toString()).isEqualTo(file2.toString());
            assertThat(actual.get(1).toString()).isEqualTo(file1.toString());
        } else if (user.getUsername().equals(user2.getUsername())) {
            assertThat(actual).hasSize(1);
            assertThat(actual.get(0).toString()).isEqualTo(file4.toString());
        } else if (user.getUsername().equals(user3.getUsername())) {
            assertThat(actual).hasSize(1);
            assertThat(actual.get(0).toString()).isEqualTo(file5.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("getUsers")
    void getLimitFilesByUsername_withoutException(User user) {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<List<StorageFile>> fileAnswer = findAllByUserAndStatusOrderByIdDescAnswer();
        Mockito.when(fileRepository.findAllByUserAndStatusOrderByIdDesc(any(User.class), any(Status.class)))
                .thenAnswer(fileAnswer);

        for (int limit = 0; limit < 10; limit++) {
            List<StorageFile> actual = cloudDBService.getLimitFilesByUsername(user.getUsername(), limit);

            if (limit >= 2) {
                if (user.getUsername().equals(user1.getUsername())) {
                    assertThat(actual).hasSize(2);
                    assertThat(actual.get(0).toString()).isEqualTo(file2.toString());
                    assertThat(actual.get(1).toString()).isEqualTo(file1.toString());
                } else if (user.getUsername().equals(user2.getUsername())) {
                    assertThat(actual).hasSize(1);
                    assertThat(actual.get(0).toString()).isEqualTo(file4.toString());
                } else if (user.getUsername().equals(user3.getUsername())) {
                    assertThat(actual).hasSize(1);
                    assertThat(actual.get(0).toString()).isEqualTo(file5.toString());
                }
            } else if (limit == 1) {
                if (user.getUsername().equals(user1.getUsername())) {
                    assertThat(actual).hasSize(1);
                    assertThat(actual.get(0).toString()).isEqualTo(file2.toString());
                } else if (user.getUsername().equals(user2.getUsername())) {
                    assertThat(actual).hasSize(1);
                    assertThat(actual.get(0).toString()).isEqualTo(file4.toString());
                } else if (user.getUsername().equals(user3.getUsername())) {
                    assertThat(actual).hasSize(1);
                    assertThat(actual.get(0).toString()).isEqualTo(file5.toString());
                }
            } else {
                assertThat(actual).isEmpty();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getUsers")
    void getLimitFilesByUsername_throwInputDataException(User user) {
        uploadDataToDB();

        for (int ii = -10; ii < 0; ii++) {
            final int limit = ii;

            InputDataException e = assertThrows(InputDataException.class, () ->
                    cloudDBService.getLimitFilesByUsername(user.getUsername(), limit));

            String message = e.getMessage();
            assertTrue(message.equals("Limit must be positive"));
        }
    }


    @Test
    void deleteFileByUsernameAndFilename() {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<Optional<StorageFile>> fileAnswer = findFileByUserAndFilenameAndStatusAnswer();
        Mockito.when(fileRepository.findFirstByUserAndFilenameAndStatus(any(User.class), anyString(), any(Status.class)))
                .thenAnswer(fileAnswer);

        Answer<StorageFile> saveFileAnswer = saveFileAnswer();
        Mockito.when(fileRepository.save(any(StorageFile.class)))
                .thenAnswer(saveFileAnswer);

        List<StorageFile> userFiles0 = user1.getUserFiles();

        assertThat(userFiles0.get(0).getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(userFiles0.get(1).getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(userFiles0.get(2).getStatus()).isEqualTo(Status.DELETED);

        // delete file1
        cloudDBService.deleteFileByUsernameAndFilename(user1.getUsername(), userFiles0.get(0).getFilename());
        List<StorageFile> userFiles1 = user1.getUserFiles();

        assertThat(userFiles1.get(0).getStatus()).isEqualTo(Status.DELETED);
        assertThat(userFiles1.get(1).getStatus()).isEqualTo(Status.ACTIVE);
        assertThat(userFiles1.get(2).getStatus()).isEqualTo(Status.DELETED);

        // delete file2
        cloudDBService.deleteFileByUsernameAndFilename(user1.getUsername(), userFiles1.get(1).getFilename());
        List<StorageFile> userFiles2 = user1.getUserFiles();

        assertThat(userFiles2.get(0).getStatus()).isEqualTo(Status.DELETED);
        assertThat(userFiles2.get(1).getStatus()).isEqualTo(Status.DELETED);
        assertThat(userFiles2.get(2).getStatus()).isEqualTo(Status.DELETED);

        // InputDataException

        InputDataException e1 = assertThrows(InputDataException.class, () ->
                cloudDBService.deleteFileByUsernameAndFilename(user1.getUsername(), userFiles1.get(2).getFilename()));

        String message1 = e1.getMessage();
        assertTrue(message1.equals("File with name " + userFiles1.get(2).getFilename() + " does not exist in storage"));

        InputDataException e2 = assertThrows(InputDataException.class, () ->
                cloudDBService.deleteFileByUsernameAndFilename(user1.getUsername(), "ngiengirengire"));

        String message2 = e2.getMessage();
        assertTrue(message2.equals("File with name " + "ngiengirengire" + " does not exist in storage"));
    }

    @Test
    void renameFile_withoutException() {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<Optional<StorageFile>> fileAnswer = findFileByUserAndFilenameAndStatusAnswer();
        Mockito.when(fileRepository.findFirstByUserAndFilenameAndStatus(any(User.class), anyString(), any(Status.class)))
                .thenAnswer(fileAnswer);

        Answer<StorageFile> saveFileAnswer = saveFileAnswer();
        Mockito.when(fileRepository.save(any(StorageFile.class)))
                .thenAnswer(saveFileAnswer);

        List<StorageFile> userFiles0 = user1.getUserFiles();

        assertThat(userFiles0.get(0).getFilename()).isEqualTo("filename1");
        assertThat(userFiles0.get(1).getFilename()).isEqualTo("filename2");
        assertThat(userFiles0.get(2).getFilename()).isEqualTo("filename3");

        cloudDBService.renameFile(user1.getUsername(), userFiles0.get(0).getFilename(), "filename1_new");
        List<StorageFile> userFiles1 = user1.getUserFiles();

        assertThat(userFiles0.get(0).getFilename()).isEqualTo("filename1_new");
        assertThat(userFiles0.get(1).getFilename()).isEqualTo("filename2");
        assertThat(userFiles0.get(2).getFilename()).isEqualTo("filename3");

        cloudDBService.renameFile(user1.getUsername(), userFiles1.get(1).getFilename(), "filename2_new");
        List<StorageFile> userFiles2 = user1.getUserFiles();

        assertThat(userFiles0.get(0).getFilename()).isEqualTo("filename1_new");
        assertThat(userFiles0.get(1).getFilename()).isEqualTo("filename2_new");
        assertThat(userFiles0.get(2).getFilename()).isEqualTo("filename3");
    }

    @Test
    void renameFile_throwInputDataException() {
        uploadDataToDB();

        Answer<Optional<User>> userAnswer = findUserByUsernameAnswer();
        Mockito.when(userRepository.findUserByUsername(anyString()))
                .thenAnswer(userAnswer);

        Answer<Optional<StorageFile>> fileAnswer = findFileByUserAndFilenameAndStatusAnswer();
        Mockito.when(fileRepository.findFirstByUserAndFilenameAndStatus(any(User.class), anyString(), any(Status.class)))
                .thenAnswer(fileAnswer);

        Answer<StorageFile> saveFileAnswer = saveFileAnswer();
        Mockito.when(fileRepository.save(any(StorageFile.class)))
                .thenAnswer(saveFileAnswer);

        List<StorageFile> userFiles0 = user1.getUserFiles();

        assertThat(userFiles0.get(0).getFilename()).isEqualTo("filename1");
        assertThat(userFiles0.get(1).getFilename()).isEqualTo("filename2");
        assertThat(userFiles0.get(2).getFilename()).isEqualTo("filename3");

        cloudDBService.renameFile(user1.getUsername(), userFiles0.get(0).getFilename(), "filename1_new");
        List<StorageFile> userFiles1 = user1.getUserFiles();

        InputDataException e1 = assertThrows(InputDataException.class, () ->
                cloudDBService.renameFile(user1.getUsername(), userFiles1.get(2).getFilename(), "1234"));

        String message1 = e1.getMessage();
        assertTrue(message1.equals("File with name " + userFiles1.get(2).getFilename() + " does not exist in storage"));

        InputDataException e2 = assertThrows(InputDataException.class, () ->
                cloudDBService.renameFile(user1.getUsername(), userFiles1.get(1).getFilename(), userFiles1.get(0).getFilename()));

        String message2 = e2.getMessage();
        assertTrue(message2.equals("File with name " + userFiles1.get(0).getFilename() + " is already exists"));

    }

    public Answer<Optional<User>> findUserByUsernameAnswer() {
        uploadDataToDB();
        List<User> userList = List.of(user1, user2, user3);

        Answer<Optional<User>> answer = new Answer<>() {
            public Optional<User> answer(InvocationOnMock invocation) {
                String string = invocation.getArgument(0, String.class);

                for (int ii = 0; ii < userList.size(); ii++) {
                    User user = userList.get(ii);
                    String username = user.getUsername();

                    if (string.equals(username))
                        return Optional.of(user);
                }

                return Optional.empty();
            }
        };
        return answer;
    }

    public Answer<Optional<StorageFile>> findFileByUserAndFilenameAndStatusAnswer() {
        uploadDataToDB();

        Answer<Optional<StorageFile>> answer = new Answer<>() {
            public Optional<StorageFile> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                User user = (User) args[0];
                String filename = (String) args[1];
                Status status = (Status) args[2];

                String username = user.getUsername();

                if (username.equals(user1.getUsername())) {
                    if (filename.equals(file1.getFilename()) && status.equals(file1.getStatus()))
                        return Optional.of(file1);

                    if (filename.equals(file2.getFilename()) && status.equals(file2.getStatus()))
                        return Optional.of(file2);

                    if (filename.equals(file3.getFilename()) && status.equals(file3.getStatus()))
                        return Optional.of(file3);
                } else if (username.equals(user2.getUsername())) {
                    if (filename.equals(file4.getFilename()) && status.equals(file4.getStatus()))
                        return Optional.of(file4);
                } else if (username.equals(user3.getUsername())) {
                    if (filename.equals(file5.getFilename()) && status.equals(file5.getStatus()))
                        return Optional.of(file5);
                }
                return Optional.empty();
            }
        };
        return answer;
    }

    public Answer<List<StorageFile>> findAllByUserAndStatusOrderByIdDescAnswer() {
        Answer<List<StorageFile>> answer = new Answer<>() {
            public List<StorageFile> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                User user = (User) args[0];
                Status status = (Status) args[1];

                List<StorageFile> userFiles = user.getUserFiles();
                List<StorageFile> userFilesWithStatus = new ArrayList<>();
                for (int ii = (userFiles.size() - 1); ii >= 0; ii--) {
                    StorageFile file = userFiles.get(ii);
                    if (file.getStatus().equals(status))
                        userFilesWithStatus.add(file);
                }
                return userFilesWithStatus;
            }
        };
        return answer;
    }

    public Answer<StorageFile> saveFileAnswer() {
        uploadDataToDB();
        List<StorageFile> fileList = List.of(file1, file2, file3, file4, file5);

        Answer<StorageFile> answer = new Answer<>() {
            public StorageFile answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                StorageFile newFile = (StorageFile) args[0];

                int fileId = newFile.getId().intValue();
                if (fileId <= fileList.size() && fileId > 0) {
                    fileList.get(fileId-1).setFilename(newFile.getFilename());
                    fileList.get(fileId-1).setFileSize(newFile.getFileSize());
                    fileList.get(fileId-1).setStatus(newFile.getStatus());
                    fileList.get(fileId-1).setUpdated(newFile.getUpdated());
                    fileList.get(fileId-1).setUser(newFile.getUser());

                    user1.setUserFiles(List.of(file1, file2, file3));
                    user2.setUserFiles(List.of(file4));
                    user3.setUserFiles(List.of(file5));
                }
                return newFile;
            }
        };
        return answer;
    }


    public static void uploadDataToDB() {

        // creating user1, add to users
        user1 = new User();
        user1.setUsername("user1@gmail.com");
        user1.setFirstName("firstname1");
        user1.setLastName("lastname1");
        user1.setPassword("password1");
        user1.setUserFiles(new ArrayList<>());
        user1.setId(1L);
        user1.setCreated(new Date());
        user1.setUpdated(new Date());
        user1.setStatus(Status.ACTIVE);

        // creating user2, add to users
        user2 = new User();
        user2.setUsername("user2@gmail.com");
        user2.setFirstName("firstname2");
        user2.setLastName("lastname2");
        user2.setPassword("password2");
        user2.setUserFiles(new ArrayList<>());
        user2.setId(2L);
        user2.setCreated(new Date());
        user2.setUpdated(new Date());
        user2.setStatus(Status.ACTIVE);

        // creating user3, add to users
        user3 = new User();
        user3.setUsername("user3@gmail.com");
        user3.setFirstName("firstname3");
        user3.setLastName("lastname3");
        user3.setPassword("password3");
        user3.setUserFiles(new ArrayList<>());
        user3.setId(3L);
        user3.setCreated(new Date());
        user3.setUpdated(new Date());
        user3.setStatus(Status.ACTIVE);

        // creating file1, add to files (user1)
        file1 = new StorageFile();
        file1.setFilename("filename1");
        file1.setFileSize(1234);
        file1.setCreated(new Date());
        file1.setUpdated(new Date());
        file1.setStatus(Status.ACTIVE);
        file1.setUser(user1);
        file1.setId(1L);

        // creating file2, add to files (user1)
        file2 = new StorageFile();
        file2.setFilename("filename2");
        file2.setFileSize(2345);
        file2.setCreated(new Date());
        file2.setUpdated(new Date());
        file2.setStatus(Status.ACTIVE);
        file2.setUser(user1);
        file2.setId(2L);

        // creating file3, add to files (user1)
        file3 = new StorageFile();
        file3.setFilename("filename3");
        file3.setFileSize(3456);
        file3.setCreated(new Date());
        file3.setUpdated(new Date());
        file3.setStatus(Status.DELETED);
        file3.setUser(user1);
        file3.setId(3L);

        // creating file4, add to files (user2)
        file4 = new StorageFile();
        file4.setFilename("filename3");
        file4.setFileSize(3456);
        file4.setCreated(new Date());
        file4.setUpdated(new Date());
        file4.setStatus(Status.ACTIVE);
        file4.setUser(user2);
        file4.setId(4L);

        // creating file5, add to files (user3)
        file5 = new StorageFile();
        file5.setFilename("filename3");
        file5.setFileSize(3456);
        file5.setCreated(new Date());
        file5.setUpdated(new Date());
        file5.setStatus(Status.ACTIVE);
        file5.setUser(user3);
        file5.setId(5L);

        user1.setUserFiles(List.of(file1, file2, file3));
        user2.setUserFiles(List.of(file4));
        user3.setUserFiles(List.of(file5));
    }
}