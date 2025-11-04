package com.cassini.graph.service.impl;

import com.cassini.graph.entity.Node;
import com.cassini.graph.repository.NodeRepository;
import com.cassini.graph.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NodeServiceImpl implements NodeService {

    @Autowired
    private NodeRepository nodeRepository;

    @Override
    public List<Node> getAllNodes() {
        return nodeRepository.findAll();
    }

    @Override
    public Optional<Node> getNodeById(Integer id) {
        return nodeRepository.findById(id);
    }

    @Override
    public List<Node> getNodesByThemeId(Integer themeId) {
        return nodeRepository.findByThemeId(themeId);
    }

    @Override
    public Node saveNode(Node node) {
        return nodeRepository.save(node);
    }

    @Override
    public void deleteNode(Integer id) {
        nodeRepository.deleteById(id);
    }
}
