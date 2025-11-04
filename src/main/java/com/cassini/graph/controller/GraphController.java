package com.cassini.graph.controller;

import com.cassini.graph.entity.Edge;
import com.cassini.graph.entity.Node;
import com.cassini.graph.service.EdgeService;
import com.cassini.graph.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/graph")
public class GraphController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private EdgeService edgeService;

    @GetMapping("/full")
    public ResponseEntity<Map<String, Object>> getFullGraph() {
        List<Node> nodes = nodeService.getAllNodes();
        List<Edge> edges = edgeService.getAllEdges();
        
        // 添加详细的调试日志
        System.out.println("===== 后端 getFullGraph 方法调试 =====");
        System.out.println("总节点数: " + nodes.size());
        
        // 统计各主题节点数量
        Map<Integer, Integer> themeStats = new HashMap<>();
        for (Node node : nodes) {
            Integer themeId = node.getTheme().getId();
            themeStats.put(themeId, themeStats.getOrDefault(themeId, 0) + 1);
        }
        System.out.println("主题ID分布统计:");
        for (Map.Entry<Integer, Integer> entry : themeStats.entrySet()) {
            System.out.println("  主题ID " + entry.getKey() + ": " + entry.getValue() + " 个节点");
        }
        
        // 特别检查theme_id=4的节点
        List<Node> theme4Nodes = nodes.stream()
            .filter(node -> node.getTheme().getId() == 4)
            .collect(java.util.stream.Collectors.toList());
        System.out.println("theme_id=4的节点数量: " + theme4Nodes.size());
        if (!theme4Nodes.isEmpty()) {
            System.out.println("theme_id=4的节点示例: ");
            theme4Nodes.stream().limit(5).forEach(node -> 
                System.out.println("  - " + node.getId() + ": " + node.getName())
            );
        }
        
        System.out.println("总边数: " + edges.size());
        System.out.println("====================================\n");
        
        Map<String, Object> graphData = new HashMap<>();
        graphData.put("nodes", nodes);
        graphData.put("edges", edges);
        
        return new ResponseEntity<>(graphData, HttpStatus.OK);
    }

    @GetMapping("/theme/{themeId}")
    public ResponseEntity<Map<String, Object>> getGraphByTheme(@PathVariable Integer themeId) {
        List<Node> nodes = nodeService.getNodesByThemeId(themeId);
        
        // 创建节点名称集合，用于快速判断边是否与节点相关
        Set<String> nodeNames = new HashSet<>();
        for (Node node : nodes) {
            nodeNames.add(node.getName());
        }
        
        // 获取这些节点相关的所有边
        List<Edge> allEdges = edgeService.getAllEdges();
        List<Edge> themeEdges = new ArrayList<>();
        for (Edge edge : allEdges) {
            String sourceNodeName = edge.getSourceNodeName();
            String targetNodeName = edge.getTargetNodeName();
            
            // 检查边的源节点或目标节点是否在当前主题的节点集合中
            if (nodeNames.contains(sourceNodeName) || nodeNames.contains(targetNodeName)) {
                themeEdges.add(edge);
            }
        }

        Map<String, Object> graphData = new HashMap<>();
        graphData.put("nodes", nodes);
        graphData.put("edges", themeEdges);
        
        return new ResponseEntity<>(graphData, HttpStatus.OK);
    }
}
