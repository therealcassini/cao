package com.cassini.graph.controller;

import com.cassini.graph.entity.SearchConfig;
import com.cassini.graph.service.SearchConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/search-configs")
public class SearchConfigController {
    
    @Autowired
    private SearchConfigService searchConfigService;
    
    /**
     * 获取所有搜索配置
     */
    @GetMapping
    public ResponseEntity<List<SearchConfig>> getAllSearchConfigs() {
        List<SearchConfig> configs = searchConfigService.getAllSearchConfigs();
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }
    
    /**
     * 根据ID获取搜索配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<SearchConfig> getSearchConfigById(@PathVariable Integer id) {
        Optional<SearchConfig> config = searchConfigService.getSearchConfigById(id);
        return config.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    /**
     * 根据数据库名和表名获取搜索配置
     */
    @GetMapping("/database/{database}/table/{table}")
    public ResponseEntity<List<SearchConfig>> getSearchConfigsByDatabaseAndTable(
            @PathVariable String database,
            @PathVariable String table) {
        List<SearchConfig> configs = searchConfigService.getSearchConfigsByDatabaseAndTable(database, table);
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }
    
    /**
     * 根据数据库名获取搜索配置
     */
    @GetMapping("/database/{database}")
    public ResponseEntity<List<SearchConfig>> getSearchConfigsByDatabase(
            @PathVariable String database) {
        List<SearchConfig> configs = searchConfigService.getSearchConfigsByDatabase(database);
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }
    
    /**
     * 创建搜索配置
     */
    @PostMapping
    public ResponseEntity<SearchConfig> createSearchConfig(@RequestBody SearchConfig searchConfig) {
        // 验证必要字段
        if (searchConfig.getDatabaseName() == null || searchConfig.getDatabaseName().trim().isEmpty() ||
            searchConfig.getTableName() == null || searchConfig.getTableName().trim().isEmpty() ||
            searchConfig.getSearchFields() == null || searchConfig.getSearchFields().trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        SearchConfig savedConfig = searchConfigService.saveSearchConfig(searchConfig);
        return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
    }
    
    /**
     * 更新搜索配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<SearchConfig> updateSearchConfig(
            @PathVariable Integer id,
            @RequestBody SearchConfig searchConfig) {
        if (!searchConfigService.getSearchConfigById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // 验证必要字段
        if (searchConfig.getDatabaseName() == null || searchConfig.getDatabaseName().trim().isEmpty() ||
            searchConfig.getTableName() == null || searchConfig.getTableName().trim().isEmpty() ||
            searchConfig.getSearchFields() == null || searchConfig.getSearchFields().trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        searchConfig.setId(id);
        SearchConfig updatedConfig = searchConfigService.saveSearchConfig(searchConfig);
        return new ResponseEntity<>(updatedConfig, HttpStatus.OK);
    }
    
    /**
     * 删除搜索配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearchConfig(@PathVariable Integer id) {
        if (!searchConfigService.getSearchConfigById(id).isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        searchConfigService.deleteSearchConfig(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}