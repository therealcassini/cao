package com.cassini.graph.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "theme")
@Data
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_node_color", length = 20)
    private String defaultNodeColor = "#4285F4";

    @Column(name = "default_node_size")
    private Integer defaultNodeSize = 20;

    @Column(name = "default_node_icon", length = 100)
    private String defaultNodeIcon = "📝";

    @Column(name = "default_edge_color", length = 20)
    private String defaultEdgeColor = "#999999";

    @Column(name = "default_edge_width")
    private Integer defaultEdgeWidth = 2;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;
}
