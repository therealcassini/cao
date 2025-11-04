package com.cassini.graph.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
    
    @GetMapping("/connection")
    public Map<String, Object> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "后端服务连接成功！");
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
    
    @GetMapping("/sample-graph")
    public Map<String, Object> getSampleGraph() {
        Map<String, Object> response = new HashMap<>();
        
        // 模拟节点数据
        List<Map<String, Object>> nodes = new ArrayList<>();
        
        Map<String, Object> node1 = new HashMap<>();
        node1.put("id", 1);
        node1.put("name", "测试节点1");
        node1.put("themeId", 1);
        nodes.add(node1);
        
        Map<String, Object> node2 = new HashMap<>();
        node2.put("id", 2);
        node2.put("name", "测试节点2");
        node2.put("themeId", 1);
        nodes.add(node2);
        
        response.put("nodes", nodes);
        
        // 模拟边数据
        List<Map<String, Object>> edges = new ArrayList<>();
        
        Map<String, Object> edge1 = new HashMap<>();
        edge1.put("id", 1);
        edge1.put("sourceNodeName", "测试节点1");
        edge1.put("targetNodeName", "测试节点2");
        edge1.put("relation", "关联关系");
        edges.add(edge1);
        
        response.put("edges", edges);
        
        return response;
    }
}