package ru.netology.diploma.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;

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
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public StorageFile(String filename, @Min(1) int fileSize, User user) {
        this.filename = filename;
        this.fileSize = fileSize;
        this.user = user;
    }
}
