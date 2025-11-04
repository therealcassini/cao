package com.cassini.graph.controller;

import com.cassini.graph.entity.Edge;
import com.cassini.graph.service.EdgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/edges")
public class EdgeController {

    @Autowired
    private EdgeService edgeService;

    @GetMapping
    public ResponseEntity<List<Edge>> getAllEdges() {
        List<Edge> edges = edgeService.getAllEdges();
        return new ResponseEntity<>(edges, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Edge> getEdgeById(@PathVariable Integer id) {
        Optional<Edge> edge = edgeService.getEdgeById(id);
        return edge.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/node/{nodeId}")
    public ResponseEntity<Map<String, Object>> getEdgesByNodeId(@PathVariable Integer nodeId) {
        // 数据库结构已变更，不再支持根据ID获取边
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "数据库结构已变更");
        errorResponse.put("message", "请使用/api/edges/node-name/{nodeName}端点代替");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/node-name/{nodeName}")
    public ResponseEntity<List<Edge>> getEdgesByNodeName(@PathVariable String nodeName) {
        List<Edge> edges = edgeService.getEdgesByNodeName(nodeName);
        return new ResponseEntity<>(edges, HttpStatus.OK);
    }

    @GetMapping("/theme/{themeId}")
    public ResponseEntity<List<Edge>> getEdgesByThemeId(@PathVariable Integer themeId) {
        List<Edge> edges = edgeService.getEdgesByThemeId(themeId);
        return new ResponseEntity<>(edges, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Edge> createEdge(@RequestBody Edge edge) {
        Edge savedEdge = edgeService.saveEdge(edge);
        return new ResponseEntity<>(savedEdge, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Edge> updateEdge(@PathVariable Integer id, @RequestBody Edge edge) {
        if (!edgeService.getEdgeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        edge.setId(id);
        Edge updatedEdge = edgeService.saveEdge(edge);
        return new ResponseEntity<>(updatedEdge, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEdge(@PathVariable Integer id) {
        if (!edgeService.getEdgeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        edgeService.deleteEdge(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
