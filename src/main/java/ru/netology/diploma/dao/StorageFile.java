package ru.netology.diploma.dao;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Min;

@Entity
@Table(name = "files")
@Data
public class StorageFile extends BaseDaoEntity {

    @Column(name = "file_name", nullable = false)
    private String filename;

    @Column(name = "file_size", nullable = false)
    @Min(1)
    private int fileSize;

    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "body", nullable = false)
    private String body;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}
