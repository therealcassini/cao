package com.cassini.graph.service;

import com.cassini.graph.entity.Edge;
import java.util.List;
import java.util.Optional;

public interface EdgeService {
    List<Edge> getAllEdges();
    Optional<Edge> getEdgeById(Integer id);
    List<Edge> getEdgesByNodeId(Integer nodeId);
    List<Edge> getEdgesByNodeName(String nodeName);
    List<Edge> getEdgesByThemeId(Integer themeId);
    Edge saveEdge(Edge edge);
    void deleteEdge(Integer id);
}
