package com.cassini.graph.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "node")
@Data
public class Node {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "properties", columnDefinition = "JSON")
    private String properties;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "size")
    private Integer size;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "x")
    private Double x;

    @Column(name = "y")
    private Double y;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
