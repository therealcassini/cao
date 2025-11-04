package com.cassini.graph.service;

import com.cassini.graph.entity.Node;
import java.util.List;
import java.util.Optional;

public interface NodeService {
    List<Node> getAllNodes();
    Optional<Node> getNodeById(Integer id);
    List<Node> getNodesByThemeId(Integer themeId);
    Node saveNode(Node node);
    void deleteNode(Integer id);
}
