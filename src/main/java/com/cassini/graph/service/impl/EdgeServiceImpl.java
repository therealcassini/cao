package com.cassini.graph.service.impl;

import com.cassini.graph.entity.Edge;
import com.cassini.graph.repository.EdgeRepository;
import com.cassini.graph.service.EdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EdgeServiceImpl implements EdgeService {

    @Autowired
    private EdgeRepository edgeRepository;

    @Override
    public List<Edge> getAllEdges() {
        return edgeRepository.findAll();
    }

    @Override
    public Optional<Edge> getEdgeById(Integer id) {
        return edgeRepository.findById(id);
    }

    @Override
    public List<Edge> getEdgesByNodeId(Integer nodeId) {
        // 注意：由于数据库结构已变更，此方法不再适用，请使用getEdgesByNodeName
        throw new UnsupportedOperationException("请使用getEdgesByNodeName方法");
    }

    @Override
    public List<Edge> getEdgesByNodeName(String nodeName) {
        return edgeRepository.findBySourceNodeNameOrTargetNodeName(nodeName, nodeName);
    }

    @Override
    public List<Edge> getEdgesByThemeId(Integer themeId) {
        return edgeRepository.findByThemeId(themeId);
    }

    @Override
    public Edge saveEdge(Edge edge) {
        return edgeRepository.save(edge);
    }

    @Override
    public void deleteEdge(Integer id) {
        edgeRepository.deleteById(id);
    }
}
