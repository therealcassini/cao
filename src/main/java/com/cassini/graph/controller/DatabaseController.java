package com.cassini.graph.controller;

import com.cassini.graph.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database")
public class DatabaseController {

    @Autowired
    private DatabaseService databaseService;

    /**
     * 获取所有数据库列表
     */
    @GetMapping("/databases")
    public List<Map<String, Object>> getDatabases() {
        return databaseService.getDatabases();
    }

    /**
     * 获取指定数据库下的所有表
     */
    @GetMapping("/databases/{database}/tables")
    public List<Map<String, Object>> getTables(@PathVariable String database) {
        return databaseService.getTables(database);
    }

    /**
     * 获取表的结构信息
     */
    @GetMapping("/tables/{table}/structure")
    public List<Map<String, Object>> getTableStructure(@PathVariable String table) {
        return databaseService.getTableStructure(table);
    }

    /**
     * 获取表的数据列表
     */
    @GetMapping("/tables/{table}/data")
    public Map<String, Object> getTableData(
            @PathVariable String table,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int pageSize,
            @RequestParam Map<String, String> allParams) {
        // 过滤掉分页相关参数和其他非搜索参数，只保留search.开头的参数
        Map<String, String> searchParams = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (key.startsWith("search.")) {
                searchParams.put(key, value);
            }
        });
        
        // 调用支持搜索的服务方法
        return databaseService.getTableData(table, page, pageSize, searchParams);
    }

    /**
     * 删除表中的记录
     */
    @DeleteMapping("/tables/{table}/data/{id}")
    public boolean deleteRecord(@PathVariable String table, @PathVariable Object id) {
        return databaseService.deleteRecord(table, id);
    }

    /**
     * 新增记录
     */
    @PostMapping("/tables/{table}/data")
    public boolean addRecord(@PathVariable String table, @RequestBody Map<String, Object> record) {
        return databaseService.addRecord(table, record);
    }

    /**
     * 更新记录
     */
    @PutMapping("/tables/{table}/data/{id}")
    public boolean updateRecord(
            @PathVariable String table,
            @PathVariable Object id,
            @RequestBody Map<String, Object> record) {
        return databaseService.updateRecord(table, id, record);
    }
}