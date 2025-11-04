package com.cassini.graph.repository;

import com.cassini.graph.entity.Node;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NodeRepository extends JpaRepository<Node, Integer> {
    List<Node> findByThemeId(Integer themeId);
}
