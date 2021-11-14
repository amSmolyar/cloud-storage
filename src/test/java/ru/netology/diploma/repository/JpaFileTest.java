package ru.netology.diploma.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.test.context.junit4.SpringRunner;
import ru.netology.diploma.dao.Status;
import ru.netology.diploma.dao.StorageFile;
import ru.netology.diploma.dao.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class JpaFileTest {

    @Autowired
    FileRepository fileRepository;

    @Autowired
    UserRepository userRepository;

    private User user1;
    private User user2;
    private User user3;
    private StorageFile file1;
    private StorageFile file2;
    private StorageFile file3;
    private StorageFile file4;
    private StorageFile file5;

    private static int initCnt = 0;

    @Test
    public void test_findNoFiles_emptyRepository() {
        Iterable<StorageFile> files = fileRepository.findAll();

        assertThat(files).isEmpty();
    }

    @Test
    @Modifying(clearAutomatically = true)
    public void test_save_and_findAllByUserAndStatusOrderByIdDesc() {

        uploadDataToDB();

        // user1
        List<StorageFile> userFiles1 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user1, Status.ACTIVE);

        List<String> userFilesStr1 = userFiles1.stream()
                .map(StorageFile::toString)
                .collect(Collectors.toList());

        assertThat(userFiles1).hasSize(3);
        assertThat(userFilesStr1).contains(file1.toString(), file2.toString(), file3.toString());
        assertThat(userFilesStr1).doesNotContain(file4.toString(), file5.toString());
        assertThat(userFiles1.get(0).toString()).isEqualTo(file3.toString());
        assertThat(userFiles1.get(1).toString()).isEqualTo(file2.toString());
        assertThat(userFiles1.get(2).toString()).isEqualTo(file1.toString());

        // user2
        List<StorageFile> userFiles2 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user2, Status.ACTIVE);

        List<String> userFilesStr2 = userFiles2.stream()
                .map(StorageFile::toString)
                .collect(Collectors.toList());

        assertThat(userFiles2).hasSize(1);
        assertThat(userFilesStr2).contains(file4.toString());
        assertThat(userFilesStr2).doesNotContain(file1.toString(), file2.toString(), file3.toString(), file5.toString());

        // user3
        List<StorageFile> userFiles3 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user3, Status.ACTIVE);

        List<String> userFilesStr3 = userFiles3.stream()
                .map(StorageFile::toString)
                .collect(Collectors.toList());

        assertThat(userFiles3).hasSize(1);
        assertThat(userFilesStr3).contains(file5.toString());
        assertThat(userFilesStr3).doesNotContain(file1.toString(), file2.toString(), file3.toString(), file4.toString());
    }

    @Test
    public void test_save_and_findFirstByUserAndFilenameAndStatus() {
        uploadDataToDB();

        // user1
        StorageFile userFile11 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file1.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile11).isNotNull();
        assertThat(userFile11.toString()).isEqualTo(file1.toString());

        StorageFile userFile12 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file2.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile12).isNotNull();
        assertThat(userFile12.toString()).isEqualTo(file2.toString());

        StorageFile userFile13 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file3.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile13).isNotNull();
        assertThat(userFile13.toString()).isEqualTo(file3.toString());

        StorageFile userFile14 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file4.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile14).isNotNull();
        assertThat(userFile14.toString()).isEqualTo(file3.toString());

        StorageFile userFile15 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file5.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile15).isNotNull();
        assertThat(userFile15.toString()).isEqualTo(file3.toString());

        // user2
        StorageFile userFile21 = fileRepository.findFirstByUserAndFilenameAndStatus(user2, file1.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile21).isNull();

        StorageFile userFile22 = fileRepository.findFirstByUserAndFilenameAndStatus(user2, file2.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile22).isNull();

        StorageFile userFile23 = fileRepository.findFirstByUserAndFilenameAndStatus(user2, file3.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile23).isNotNull();
        assertThat(userFile23.toString()).isEqualTo(file4.toString());

        StorageFile userFile24 = fileRepository.findFirstByUserAndFilenameAndStatus(user2, file4.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile24).isNotNull();
        assertThat(userFile24.toString()).isEqualTo(file4.toString());

        StorageFile userFile25 = fileRepository.findFirstByUserAndFilenameAndStatus(user2, file5.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile25).isNotNull();
        assertThat(userFile25.toString()).isEqualTo(file4.toString());

        // user3
        StorageFile userFile31 = fileRepository.findFirstByUserAndFilenameAndStatus(user3, file1.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile31).isNull();

        StorageFile userFile32 = fileRepository.findFirstByUserAndFilenameAndStatus(user3, file2.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile32).isNull();

        StorageFile userFile33 = fileRepository.findFirstByUserAndFilenameAndStatus(user3, file3.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile33).isNotNull();
        assertThat(userFile33.toString()).isEqualTo(file5.toString());

        StorageFile userFile34 = fileRepository.findFirstByUserAndFilenameAndStatus(user3, file4.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile34).isNotNull();
        assertThat(userFile34.toString()).isEqualTo(file5.toString());

        StorageFile userFile35 = fileRepository.findFirstByUserAndFilenameAndStatus(user3, file5.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(userFile35).isNotNull();
        assertThat(userFile35.toString()).isEqualTo(file5.toString());
    }

    @Test
    public void test_changeFile_findAllByUserAndStatusOrderByIdDesc() {
        uploadDataToDB();

        StorageFile storageFile11 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file1.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(storageFile11).isNotNull();
        storageFile11.setFilename("filename1_new");
        storageFile11.setUpdated(new Date());
        fileRepository.save(storageFile11);

        StorageFile storageFile1_newName = fileRepository.findFirstByUserAndFilenameAndStatus(user1, "filename1_new", Status.ACTIVE)
                .orElse(null);

        assertThat(storageFile1_newName).isNotNull();
        assertThat(storageFile1_newName.getId()).isEqualTo(storageFile11.getId());
        assertThat(storageFile1_newName.getUser().toString()).isEqualTo(storageFile11.getUser().toString());
        assertThat(storageFile1_newName.getFileSize()).isEqualTo(storageFile11.getFileSize());
        assertThat(storageFile1_newName.getStatus()).isEqualTo(storageFile11.getStatus());
        assertThat(storageFile1_newName.getCreated()).isEqualTo(storageFile11.getCreated());

        StorageFile storageFile12 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file1.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(storageFile12).isNull();

        // change status
        StorageFile storageFile21 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file2.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(storageFile21).isNotNull();
        storageFile21.setStatus(Status.DELETED);
        storageFile21.setUpdated(new Date());
        fileRepository.save(storageFile21);

        StorageFile storageFile22 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file2.getFilename(), Status.ACTIVE)
                .orElse(null);

        assertThat(storageFile22).isNull();

        StorageFile storageFile23 = fileRepository.findFirstByUserAndFilenameAndStatus(user1, file2.getFilename(), Status.DELETED)
                .orElse(null);

        assertThat(storageFile23).isNotNull();
        assertThat(storageFile23.getId()).isEqualTo(storageFile21.getId());
        assertThat(storageFile23.getUser().toString()).isEqualTo(storageFile21.getUser().toString());
        assertThat(storageFile23.getFileSize()).isEqualTo(storageFile21.getFileSize());
        assertThat(storageFile23.getFilename()).isEqualTo(storageFile21.getFilename());
        assertThat(storageFile23.getCreated()).isEqualTo(storageFile21.getCreated());

        // get list of files
        List<StorageFile> userFiles = fileRepository.findAllByUserAndStatusOrderByIdDesc(user1, Status.ACTIVE);

        assertThat(userFiles).hasSize(2);
        assertThat(userFiles.get(0).toString()).isEqualTo(file3.toString());
        // file1 уже изменен выше на файл storageFile11:
        assertThat(userFiles.get(1).toString()).isEqualTo(storageFile11.toString());

    }

    @Test
    public void test_deleteFile() {
        uploadDataToDB();

        List<StorageFile> userFiles1 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user1, Status.ACTIVE);

        assertThat(userFiles1).hasSize(3);
        assertThat(userFiles1.get(0).toString()).isEqualTo(file3.toString());
        assertThat(userFiles1.get(1).toString()).isEqualTo(file2.toString());
        assertThat(userFiles1.get(2).toString()).isEqualTo(file1.toString());

        fileRepository.delete(file2);

        List<StorageFile> userFiles2 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user1, Status.ACTIVE);

        assertThat(userFiles2).hasSize(2);
        assertThat(userFiles2.get(0).toString()).isEqualTo(file3.toString());
        assertThat(userFiles2.get(1).toString()).isEqualTo(file1.toString());

        fileRepository.delete(file3);

        List<StorageFile> userFiles3 = fileRepository.findAllByUserAndStatusOrderByIdDesc(user1, Status.ACTIVE);

        assertThat(userFiles3).hasSize(1);
        assertThat(userFiles3.get(0).toString()).isEqualTo(file1.toString());

    }

    public void uploadDataToDB() {
        userRepository.deleteAll();
        fileRepository.deleteAll();

        // creating user1, add to users
        user1 = new User();
        user1.setUsername("user1@gmail.com");
        user1.setFirstName("firstname1");
        user1.setLastName("lastname1");
        user1.setPassword("password1");
        user1.setUserFiles(new ArrayList<>());
        user1.setId(initCnt*3 + 1L);
        user1.setCreated(new Date());
        user1.setUpdated(new Date());
        user1.setStatus(Status.ACTIVE);
        userRepository.save(user1);

        // creating user2, add to users
        user2 = new User();
        user2.setUsername("user2@gmail.com");
        user2.setFirstName("firstname2");
        user2.setLastName("lastname2");
        user2.setPassword("password2");
        user2.setUserFiles(new ArrayList<>());
        user2.setId(initCnt*3 + 2L);
        user2.setCreated(new Date());
        user2.setUpdated(new Date());
        user2.setStatus(Status.ACTIVE);
        userRepository.save(user2);

        // creating user3, add to users
        user3 = new User();
        user3.setUsername("user3@gmail.com");
        user3.setFirstName("firstname3");
        user3.setLastName("lastname3");
        user3.setPassword("password3");
        user3.setUserFiles(new ArrayList<>());
        user3.setId(initCnt*3 + 3L);
        user3.setCreated(new Date());
        user3.setUpdated(new Date());
        user3.setStatus(Status.ACTIVE);
        userRepository.save(user3);

        // creating file1, add to files (user1)
        file1 = new StorageFile();
        file1.setFilename("filename1");
        file1.setFileSize(1234);
        file1.setCreated(new Date());
        file1.setUpdated(new Date());
        file1.setStatus(Status.ACTIVE);
        file1.setUser(user1);
        file1.setId(initCnt*5 + 1L);
        fileRepository.save(file1);

        // creating file2, add to files (user1)
        file2 = new StorageFile();
        file2.setFilename("filename2");
        file2.setFileSize(2345);
        file2.setCreated(new Date());
        file2.setUpdated(new Date());
        file2.setStatus(Status.ACTIVE);
        file2.setUser(user1);
        file2.setId(initCnt*5 + 2L);
        fileRepository.save(file2);

        // creating file3, add to files (user1)
        file3 = new StorageFile();
        file3.setFilename("filename3");
        file3.setFileSize(3456);
        file3.setCreated(new Date());
        file3.setUpdated(new Date());
        file3.setStatus(Status.ACTIVE);
        file3.setUser(user1);
        file3.setId(initCnt*5 + 3L);
        fileRepository.save(file3);

        // creating file4, add to files (user2)
        file4 = new StorageFile();
        file4.setFilename("filename3");
        file4.setFileSize(3456);
        file4.setCreated(new Date());
        file4.setUpdated(new Date());
        file4.setStatus(Status.ACTIVE);
        file4.setUser(user2);
        file4.setId(initCnt*5 + 4L);
        fileRepository.save(file4);

        // creating file5, add to files (user3)
        file5 = new StorageFile();
        file5.setFilename("filename3");
        file5.setFileSize(3456);
        file5.setCreated(new Date());
        file5.setUpdated(new Date());
        file5.setStatus(Status.ACTIVE);
        file5.setUser(user3);
        file5.setId(initCnt*5 + 5L);
        fileRepository.save(file5);

        initCnt++;
    }



}
