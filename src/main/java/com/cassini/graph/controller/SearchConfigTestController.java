package com.cassini.graph.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search-config-test")
public class SearchConfigTestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public String test() {
        return "Search Config Test Successful";
    }
    
    @GetMapping("/db-check")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试数据库连接
            jdbcTemplate.execute("SELECT 1");
            result.put("connection", "success");
            
            // 检查search_config表是否存在
            List<String> tables = jdbcTemplate.queryForList(
                "SHOW TABLES LIKE 'search_config'", String.class
            );
            result.put("tableExists", !tables.isEmpty());
            
            // 如果表存在，检查表结构
            if (!tables.isEmpty()) {
                List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                    "DESCRIBE search_config"
                );
                result.put("columns", columns);
                
                // 尝试查询数据
                try {
                    int count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM search_config", Integer.class
                    );
                    result.put("recordCount", count);
                } catch (Exception e) {
                    result.put("queryError", e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("connection", "failed");
        }
        
        return result;
    }
}