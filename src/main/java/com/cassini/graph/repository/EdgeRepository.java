package com.cassini.graph.repository;

import com.cassini.graph.entity.Edge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, Integer> {
    List<Edge> findBySourceNodeNameOrTargetNodeName(String sourceNodeName, String targetNodeName);
    List<Edge> findByThemeId(Integer themeId);
}
