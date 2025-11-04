package com.cassini.graph.service;

import com.cassini.graph.entity.SearchConfig;

import java.util.List;
import java.util.Optional;

public interface SearchConfigService {
    
    // 获取所有搜索配置
    List<SearchConfig> getAllSearchConfigs();
    
    // 根据ID获取搜索配置
    Optional<SearchConfig> getSearchConfigById(Integer id);
    
    // 根据数据库名和表名获取搜索配置
    List<SearchConfig> getSearchConfigsByDatabaseAndTable(String databaseName, String tableName);
    
    // 根据数据库名获取搜索配置
    List<SearchConfig> getSearchConfigsByDatabase(String databaseName);
    
    // 保存搜索配置
    SearchConfig saveSearchConfig(SearchConfig searchConfig);
    
    // 删除搜索配置
    void deleteSearchConfig(Integer id);
}