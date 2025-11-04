package com.cassini.graph.service;

import java.util.List;
import java.util.Map;

public interface DatabaseService {

    /**
     * 获取所有数据库列表
     */
    List<Map<String, Object>> getDatabases();

    /**
     * 获取指定数据库下的所有表
     */
    List<Map<String, Object>> getTables(String database);

    /**
     * 获取表的结构信息
     */
    List<Map<String, Object>> getTableStructure(String table);

    /**
     * 获取表的数据列表
     */
    Map<String, Object> getTableData(String table, int page, int pageSize);
    
    /**
     * 获取表的数据列表（支持搜索）
     */
    Map<String, Object> getTableData(String table, int page, int pageSize, Map<String, String> searchParams);

    /**
     * 删除表中的记录
     */
    boolean deleteRecord(String table, Object id);

    /**
     * 新增记录
     */
    boolean addRecord(String table, Map<String, Object> record);

    /**
     * 更新记录
     */
    boolean updateRecord(String table, Object id, Map<String, Object> record);

    /**
     * 获取表的主键字段
     */
    String getPrimaryKey(String table);
}