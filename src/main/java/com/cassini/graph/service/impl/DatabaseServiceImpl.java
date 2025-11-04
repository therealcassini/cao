package com.cassini.graph.service.impl;

import com.cassini.graph.service.DatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DatabaseServiceImpl implements DatabaseService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Map<String, Object>> getDatabases() {
        try {
            // 查询所有数据库
            String sql = "SHOW DATABASES";
            System.out.println("Executing SQL: " + sql);
            List<Map<String, Object>> databases = jdbcTemplate.queryForList(sql);
            
            System.out.println("Query result size: " + (databases != null ? databases.size() : 0));
            
            // 处理结果，确保每个数据库有统一的字段名
            List<Map<String, Object>> result = new ArrayList<>();
            if (databases != null && !databases.isEmpty()) {
                for (Map<String, Object> db : databases) {
                    Map<String, Object> databaseInfo = new HashMap<>();
                    // MySQL返回的字段名可能是"Database"或"schema_name"等
                    for (Map.Entry<String, Object> entry : db.entrySet()) {
                        System.out.println("Database field: " + entry.getKey() + " = " + entry.getValue());
                        databaseInfo.put("name", entry.getValue());
                        break;
                    }
                    result.add(databaseInfo);
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error in getDatabases method:");
            e.printStackTrace();
            // 返回空列表，防止前端出错
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> getTables(String database) {
        // 切换到指定数据库
        jdbcTemplate.execute("USE " + database);
        
        // 查询所有表
        String sql = "SHOW TABLES";
        List<Map<String, Object>> tables = jdbcTemplate.queryForList(sql);
        
        // 处理结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> table : tables) {
            Map<String, Object> tableInfo = new HashMap<>();
            for (Map.Entry<String, Object> entry : table.entrySet()) {
                String tableName = entry.getValue().toString();
                tableInfo.put("name", tableName);
                
                // 获取表的注释
                String commentSql = "SELECT table_comment FROM INFORMATION_SCHEMA.TABLES WHERE table_schema = ? AND table_name = ?";
                List<Map<String, Object>> commentResult = jdbcTemplate.queryForList(commentSql, database, tableName);
                if (!commentResult.isEmpty()) {
                    String comment = commentResult.get(0).get("table_comment").toString();
                    tableInfo.put("desc", comment.isEmpty() ? tableName : comment);
                } else {
                    tableInfo.put("desc", tableName);
                }
                break;
            }
            result.add(tableInfo);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getTableStructure(String table) {
        // 使用DESCRIBE语句获取表结构
        String sql = "DESCRIBE " + table;
        List<Map<String, Object>> structure = jdbcTemplate.queryForList(sql);
        
        // 处理结果，添加额外信息
        for (Map<String, Object> field : structure) {
            // 检查是否为主键
            String key = field.get("Key") != null ? field.get("Key").toString() : "";
            field.put("isPrimaryKey", "PRI".equals(key));
        }
        
        return structure;
    }

    @Override
    public Map<String, Object> getTableData(String table, int page, int pageSize) {
        // 调用支持搜索的方法，不传入搜索参数
        return getTableData(table, page, pageSize, null);
    }
    
    @Override
    public Map<String, Object> getTableData(String table, int page, int pageSize, Map<String, String> searchParams) {
        try {
            // 计算偏移量
            int offset = (page - 1) * pageSize;
            
            // 构建查询语句和参数
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM ").append(table);
            StringBuilder countSqlBuilder = new StringBuilder("SELECT COUNT(*) FROM ").append(table);
            List<Object> params = new ArrayList<>();
            
            // 添加搜索条件
            if (searchParams != null && !searchParams.isEmpty()) {
                sqlBuilder.append(" WHERE");
                countSqlBuilder.append(" WHERE");
                boolean firstCondition = true;
                
                for (Map.Entry<String, String> entry : searchParams.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue();
                    
                    // 处理search.前缀的参数，用于模糊搜索
                    if (paramName.startsWith("search.")) {
                        String fieldName = paramName.substring(7); // 移除search.前缀
                        if (firstCondition) {
                            firstCondition = false;
                        } else {
                            sqlBuilder.append(" AND");
                            countSqlBuilder.append(" AND");
                        }
                        
                        // 使用LIKE进行模糊匹配
                        sqlBuilder.append(" " + fieldName + " LIKE ?");
                        countSqlBuilder.append(" " + fieldName + " LIKE ?");
                        params.add(paramValue);
                    }
                }
            }
            
            // 获取主键字段进行排序
            String primaryKey = getPrimaryKey(table);
            if (primaryKey != null && !primaryKey.isEmpty()) {
                sqlBuilder.append(" ORDER BY " + primaryKey);
            }
            
            // 添加分页限制
            sqlBuilder.append(" LIMIT " + offset + ", " + pageSize);
            
            // 查询总记录数
            Long total;
            if (params.isEmpty()) {
                total = jdbcTemplate.queryForObject(countSqlBuilder.toString(), Long.class);
            } else {
                total = jdbcTemplate.queryForObject(countSqlBuilder.toString(), params.toArray(), Long.class);
            }
            
            // 查询数据
            List<Map<String, Object>> data;
            if (params.isEmpty()) {
                data = jdbcTemplate.queryForList(sqlBuilder.toString());
            } else {
                data = jdbcTemplate.queryForList(sqlBuilder.toString(), params.toArray());
            }
            
            // 返回分页结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("data", data);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // 返回空结果集，防止前端出错
            Map<String, Object> result = new HashMap<>();
            result.put("total", 0);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("data", new ArrayList<>());
            return result;
        }
    }

    @Override
    public boolean deleteRecord(String table, Object id) {
        try {
            // 获取主键字段
            String primaryKey = getPrimaryKey(table);
            if (primaryKey == null || primaryKey.isEmpty()) {
                System.err.println("Cannot delete record: Table '" + table + "' has no primary key field.");
                return false;
            }
            
            // 构建删除语句
            String sql = "DELETE FROM " + table + " WHERE " + primaryKey + " = ?";
            int rowsAffected = jdbcTemplate.update(sql, id);
            
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean addRecord(String table, Map<String, Object> record) {
        try {
            // 构建插入语句
            StringBuilder sqlBuilder = new StringBuilder("INSERT INTO " + table + " (");
            StringBuilder valuesBuilder = new StringBuilder(" VALUES (");
            
            List<Object> params = new ArrayList<>();
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                // 跳过id字段
                if ("id".equalsIgnoreCase(entry.getKey()) && (entry.getValue() == null || entry.getValue().toString().isEmpty())) {
                    continue;
                }
                
                if (!first) {
                    sqlBuilder.append(", ");
                    valuesBuilder.append(", ");
                }
                sqlBuilder.append(entry.getKey());
                valuesBuilder.append("?");
                params.add(entry.getValue());
                first = false;
            }
            
            sqlBuilder.append(")").append(valuesBuilder).append(")");
            
            // 执行插入
            int rowsAffected = jdbcTemplate.update(sqlBuilder.toString(), params.toArray());
            
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateRecord(String table, Object id, Map<String, Object> record) {
        try {
            // 获取主键字段
            String primaryKey = getPrimaryKey(table);
            if (primaryKey == null || primaryKey.isEmpty()) {
                System.err.println("Cannot update record: Table '" + table + "' has no primary key field.");
                return false;
            }
            
            // 构建更新语句
            StringBuilder sqlBuilder = new StringBuilder("UPDATE " + table + " SET ");
            
            List<Object> params = new ArrayList<>();
            boolean first = true;
            
            for (Map.Entry<String, Object> entry : record.entrySet()) {
                // 跳过id字段和主键字段
                if ("id".equalsIgnoreCase(entry.getKey()) || primaryKey.equalsIgnoreCase(entry.getKey())) {
                    continue;
                }
                
                if (!first) {
                    sqlBuilder.append(", ");
                }
                sqlBuilder.append(entry.getKey()).append(" = ?");
                params.add(entry.getValue());
                first = false;
            }
            
            // 如果没有要更新的字段，直接返回成功
            if (params.isEmpty()) {
                return true;
            }
            
            sqlBuilder.append(" WHERE " + primaryKey + " = ?");
            params.add(id);
            
            // 执行更新
            int rowsAffected = jdbcTemplate.update(sqlBuilder.toString(), params.toArray());
            
            return rowsAffected > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getPrimaryKey(String table) {
        try {
            // 查询表的主键
            String sql = "SHOW KEYS FROM " + table + " WHERE Key_name = 'PRIMARY'";
            List<Map<String, Object>> keys = jdbcTemplate.queryForList(sql);
            
            if (!keys.isEmpty() && keys.get(0).containsKey("Column_name") && keys.get(0).get("Column_name") != null) {
                return keys.get(0).get("Column_name").toString();
            }
            
            // 如果没有明确的主键，尝试查找id字段
            String descSql = "DESCRIBE " + table;
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(descSql);
            
            if (columns != null) {
                for (Map<String, Object> column : columns) {
                    if (column.containsKey("Field") && column.get("Field") != null) {
                        String columnName = column.get("Field").toString();
                        if ("id".equalsIgnoreCase(columnName)) {
                            return columnName;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 记录异常但不抛出，防止方法调用失败
            e.printStackTrace();
        }
        
        return null;
    }
}