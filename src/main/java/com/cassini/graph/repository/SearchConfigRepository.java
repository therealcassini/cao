package com.cassini.graph.repository;

import com.cassini.graph.entity.SearchConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchConfigRepository extends JpaRepository<SearchConfig, Integer> {
    
    // 根据数据库名和表名查询配置
    @Query(value = "SELECT * FROM data_dev.search_config WHERE database_name = :databaseName AND table_name = :tableName", nativeQuery = true)
    List<SearchConfig> findByDatabaseNameAndTableName(String databaseName, String tableName);
    
    // 根据数据库名查询配置
    List<SearchConfig> findByDatabaseName(String databaseName);
    
    // 根据ID删除配置
    void deleteById(Integer id);
    
    // 根据ID查询配置
    Optional<SearchConfig> findById(Integer id);
}