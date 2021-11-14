package ru.netology.diploma.dao;

import liquibase.pro.packaged.C;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.util.Objects;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
public class StorageFile extends BaseDaoEntity {

    @Column(name = "file_name", nullable = false)
    private String filename;

    @Column(name = "file_size", nullable = false)
    @Min(1)
    private int fileSize;

    @ManyToOne(fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public StorageFile(String filename, @Min(1) int fileSize, User user) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.user = user;
    }

    @Override
    public String toString() {
        return "StorageFile{" +
                "filename='" + filename + '\'' +
                ", fileSize=" + fileSize +
                ", user=" + user +
                '}';
    }
}
