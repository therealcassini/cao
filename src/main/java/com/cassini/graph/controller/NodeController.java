package com.cassini.graph.controller;

import com.cassini.graph.entity.Node;
import com.cassini.graph.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/nodes")
public class NodeController {

    @Autowired
    private NodeService nodeService;

    @GetMapping
    public ResponseEntity<List<Node>> getAllNodes() {
        List<Node> nodes = nodeService.getAllNodes();
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Node> getNodeById(@PathVariable Integer id) {
        Optional<Node> node = nodeService.getNodeById(id);
        return node.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/theme/{themeId}")
    public ResponseEntity<List<Node>> getNodesByThemeId(@PathVariable Integer themeId) {
        List<Node> nodes = nodeService.getNodesByThemeId(themeId);
        return new ResponseEntity<>(nodes, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Node> createNode(@RequestBody Node node) {
        Node savedNode = nodeService.saveNode(node);
        return new ResponseEntity<>(savedNode, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Node> updateNode(@PathVariable Integer id, @RequestBody Node node) {
        if (!nodeService.getNodeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        node.setId(id);
        Node updatedNode = nodeService.saveNode(node);
        return new ResponseEntity<>(updatedNode, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNode(@PathVariable Integer id) {
        if (!nodeService.getNodeById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        nodeService.deleteNode(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
