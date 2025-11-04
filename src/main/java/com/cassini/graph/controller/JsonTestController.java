package com.cassini.graph.controller;

import com.cassini.graph.entity.SearchConfig;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/json-test")
public class JsonTestController {

    /**
     * 测试JSON解析
     */
    @PostMapping("/parse")
    public Map<String, Object> testJsonParsing(@RequestBody SearchConfig searchConfig) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("received", searchConfig);
        result.put("message", "JSON parsed successfully");
        
        // 验证必要字段
        boolean hasAllFields = searchConfig.getDatabaseName() != null && 
                              !searchConfig.getDatabaseName().trim().isEmpty() &&
                              searchConfig.getTableName() != null && 
                              !searchConfig.getTableName().trim().isEmpty() &&
                              searchConfig.getSearchFields() != null && 
                              !searchConfig.getSearchFields().trim().isEmpty();
        
        result.put("hasAllRequiredFields", hasAllFields);
        
        return result;
    }
    
    /**
     * 测试简单的JSON对象解析
     */
    @PostMapping("/simple")
    public Map<String, Object> testSimpleJson(@RequestBody Map<String, Object> requestBody) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("received", requestBody);
        return result;
    }
}