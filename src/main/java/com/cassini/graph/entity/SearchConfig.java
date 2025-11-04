package com.cassini.graph.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "search_config")
@Data
public class SearchConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "database_name", nullable = false, length = 100)
    private String databaseName;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "search_fields", nullable = false, columnDefinition = "TEXT")
    private String searchFields; // 存储为JSON字符串格式

    @Column(name = "description", length = 255)
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;
}