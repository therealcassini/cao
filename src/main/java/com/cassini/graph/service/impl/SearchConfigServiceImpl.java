package com.cassini.graph.service.impl;

import com.cassini.graph.entity.SearchConfig;
import com.cassini.graph.repository.SearchConfigRepository;
import com.cassini.graph.service.SearchConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SearchConfigServiceImpl implements SearchConfigService {
    
    @Autowired
    private SearchConfigRepository searchConfigRepository;
    
    @Override
    public List<SearchConfig> getAllSearchConfigs() {
        return searchConfigRepository.findAll();
    }
    
    @Override
    public Optional<SearchConfig> getSearchConfigById(Integer id) {
        return searchConfigRepository.findById(id);
    }
    
    @Override
    public List<SearchConfig> getSearchConfigsByDatabaseAndTable(String databaseName, String tableName) {
        return searchConfigRepository.findByDatabaseNameAndTableName(databaseName, tableName);
    }
    
    @Override
    public List<SearchConfig> getSearchConfigsByDatabase(String databaseName) {
        return searchConfigRepository.findByDatabaseName(databaseName);
    }
    
    @Override
    public SearchConfig saveSearchConfig(SearchConfig searchConfig) {
        return searchConfigRepository.save(searchConfig);
    }
    
    @Override
    public void deleteSearchConfig(Integer id) {
        searchConfigRepository.deleteById(id);
    }
}