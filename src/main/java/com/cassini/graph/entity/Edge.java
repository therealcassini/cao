package com.cassini.graph.entity;

import com.cassini.graph.entity.Theme;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "edge")
@Data
public class Edge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "source_node_name", nullable = false, length = 100)
    private String sourceNodeName;

    @Column(name = "target_node_name", nullable = false, length = 100)
    private String targetNodeName;

    @Column(name = "relation", nullable = false, length = 50)
    private String relation;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "width")
    private Integer width = 2;

    @Column(name = "dashed")
    private Boolean dashed = false;

    @ManyToOne
    @JoinColumn(name = "theme_id", nullable = false)
    private Theme theme;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
