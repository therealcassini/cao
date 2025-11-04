<template>
  <div class="mysql-to-pg-converter">
    <div class="converter-container">
      <div class="input-section">
        <label for="mysql-sql">MySQL CREATE TABLE SQL:</label>
        <textarea 
          id="mysql-sql"
          v-model="mysqlSql"
          placeholder="请粘贴MySQL的CREATE TABLE语句..."
          rows="15"
          spellcheck="false"
        ></textarea>
      </div>
      
      <div class="button-section" style="text-align: right;">
        <button @click="convertToPg" class="convert-button">转换到 PostgreSQL</button>
        <button @click="clearAll" class="clear-button">清空</button>
      </div>
      
      <div class="output-section">
        <label for="pg-sql">PostgreSQL 转换结果:</label>
        <textarea 
          id="pg-sql"
          v-model="pgSql"
          placeholder="转换后的PostgreSQL SQL将显示在这里..."
          rows="15"
          spellcheck="false"
          readonly
        ></textarea>
        <button @click="copyToClipboard" class="copy-button">复制结果</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MySqlToPgConverter',
  data() {
    return {
      mysqlSql: '',
      pgSql: ''
    }
  },
  methods: {
    convertToPg() {
      if (!this.mysqlSql.trim()) {
        this.$message.error('请输入MySQL的CREATE TABLE语句');
        return;
      }
      
      try {
        this.pgSql = this.parseAndConvertMySqlToPg(this.mysqlSql);
      } catch (error) {
        this.pgSql = `转换失败: ${error.message}`;
      }
    },
    
    parseAndConvertMySqlToPg(mysqlSql) {
      let result = '';
      let pgCreateTable = '';
      let commentStatements = [];
      
      // 处理表名和表结构
      const tableMatch = mysqlSql.match(/CREATE\s+TABLE\s+`?([\w]+)`?\s*\(([^;]+)\)/i);
      if (!tableMatch) {
        throw new Error('无法识别MySQL CREATE TABLE语句');
      }
      
      const tableName = tableMatch[1];
      let columnsData = tableMatch[2];
      
      // 提取表备注
      const tableCommentMatch = mysqlSql.match(/COMMENT\s*=\s*['"]([^'"]+)['"]/i);
      if (tableCommentMatch) {
        commentStatements.push(`COMMENT ON TABLE ${tableName} IS '${tableCommentMatch[1]}';`);
      }
      
      // 处理每一列
      const columns = columnsData.split(/,\s*/);
      const pgColumns = [];
      let primaryKeyColumns = [];
      
      columns.forEach(col => {
        const trimmedCol = col.trim();
        if (!trimmedCol) return;
        
        // 检查是否是主键定义
        if (trimmedCol.match(/^PRIMARY\s+KEY/i)) {
          const pkMatch = trimmedCol.match(/PRIMARY\s+KEY\s*\(([^)]+)\)/i);
          if (pkMatch) {
            const pkCols = pkMatch[1].split(/,\s*/).map(c => {
              const colNameMatch = c.match(/`?([\w]+)`?/);
              return colNameMatch ? colNameMatch[1] : c;
            });
            primaryKeyColumns = pkCols;
          }
          return;
        }
        
        // 处理列定义
        const colDefinition = this.parseColumnDefinition(trimmedCol);
        if (colDefinition) {
          pgColumns.push(colDefinition.column);
          if (colDefinition.comment) {
            commentStatements.push(`COMMENT ON COLUMN ${tableName}.${colDefinition.name} IS '${colDefinition.comment}';`);
          }
        }
      });
      
      // 构建PostgreSQL CREATE TABLE语句
      pgCreateTable = `CREATE TABLE ${tableName} (\n`;
      pgCreateTable += pgColumns.join(',\n');
      
      // 添加主键约束
      if (primaryKeyColumns.length > 0) {
        pgCreateTable += `,\n  PRIMARY KEY (${primaryKeyColumns.join(', ')})`;
      }
      
      pgCreateTable += '\n);\n';
      
      // 组合所有SQL语句
      result += pgCreateTable;
      if (commentStatements.length > 0) {
        result += '\n-- 表和列的备注语句\n';
        result += commentStatements.join('\n');
      }
      
      return result;
    },
    
    parseColumnDefinition(columnDef) {
      // 匹配列名
      const nameMatch = columnDef.match(/^`?([\w]+)`?/);
      if (!nameMatch) return null;
      
      const colName = nameMatch[1];
      let rest = columnDef.substring(nameMatch[0].length).trim();
      
      // 匹配数据类型
      const typeMatch = rest.match(/^(\w+(?:\(\d+(?:,\s*\d+)?\))?)/i);
      if (!typeMatch) return null;
      
      const mysqlType = typeMatch[0].toLowerCase();
      const pgType = this.mapMysqlTypeToPg(mysqlType);
      
      rest = rest.substring(typeMatch[0].length).trim();
      
      // 处理NOT NULL, DEFAULT等
      let constraints = [];
      
      // 匹配NOT NULL
      if (rest.match(/^NOT\s+NULL/i)) {
        constraints.push('NOT NULL');
        rest = rest.substring(8).trim(); // "NOT NULL".length
      }
      
      // 匹配DEFAULT
      const defaultMatch = rest.match(/^DEFAULT\s+([^\s,]+)/i);
      if (defaultMatch) {
        let defaultValue = defaultMatch[1];
        // 处理特殊默认值
        if (defaultValue.toLowerCase() === 'current_timestamp') {
          defaultValue = 'CURRENT_TIMESTAMP';
        } else if (defaultValue.toLowerCase() === 'null') {
          defaultValue = 'NULL';
        } else if (/^\d+$/.test(defaultValue)) {
          // 数字保持不变
        } else {
          // 其他值加引号
          if (!defaultValue.match(/^['"]/)) {
            defaultValue = `'${defaultValue}'`;
          }
        }
        constraints.push(`DEFAULT ${defaultValue}`);
        rest = rest.substring(defaultMatch[0].length).trim();
      }
      
      // 匹配列备注
      let comment = null;
      const commentMatch = rest.match(/COMMENT\s*['"]([^'"]+)['"]/i);
      if (commentMatch) {
        comment = commentMatch[1];
      }
      
      // 构建PostgreSQL列定义
      let pgColumnDef = `  ${colName} ${pgType}`;
      if (constraints.length > 0) {
        pgColumnDef += ' ' + constraints.join(' ');
      }
      
      return {
        name: colName,
        column: pgColumnDef,
        comment: comment
      };
    },
    
    mapMysqlTypeToPg(mysqlType) {
      const typeMap = {
        // 整数类型
        'tinyint': 'SMALLINT',
        'tinyint(1)': 'BOOLEAN',
        'smallint': 'SMALLINT',
        'mediumint': 'INTEGER',
        'int': 'INTEGER',
        'integer': 'INTEGER',
        'bigint': 'BIGINT',
        
        // 小数类型
        'float': 'FLOAT',
        'double': 'DOUBLE PRECISION',
        'decimal': 'DECIMAL',
        
        // 字符串类型
        'char': 'CHAR',
        'varchar': 'VARCHAR',
        'tinytext': 'TEXT',
        'text': 'TEXT',
        'mediumtext': 'TEXT',
        'longtext': 'TEXT',
        
        // 二进制类型
        'blob': 'BYTEA',
        'mediumblob': 'BYTEA',
        'longblob': 'BYTEA',
        
        // 日期时间类型
        'date': 'DATE',
        'time': 'TIME',
        'datetime': 'TIMESTAMP',
        'timestamp': 'TIMESTAMP',
        'year': 'INTEGER',
        
        // 其他类型
        'enum': 'TEXT',
        'set': 'TEXT',
        'json': 'JSON',
        'geometry': 'GEOMETRY'
      };
      
      // 先尝试精确匹配
      if (typeMap[mysqlType]) {
        return typeMap[mysqlType];
      }
      
      // 尝试匹配基础类型（不带括号）
      const baseType = mysqlType.split('(')[0];
      if (typeMap[baseType]) {
        // 保留括号内的参数
        const paramMatch = mysqlType.match(/\(([^)]+)\)/);
        if (paramMatch) {
          return `${typeMap[baseType]}(${paramMatch[1]})`;
        }
        return typeMap[baseType];
      }
      
      // 未知类型返回原类型
      console.warn(`未知的MySQL类型: ${mysqlType}`);
      return mysqlType;
    },
    
    clearAll() {
      this.mysqlSql = '';
      this.pgSql = '';
    },
    
    copyToClipboard() {
      if (!this.pgSql.trim()) {
        this.$message.warning('没有可复制的内容');
        return;
      }
      
      navigator.clipboard.writeText(this.pgSql).then(() => {
        this.$message.success('复制成功');
      }).catch(() => {
        this.$message.error('复制失败，请手动复制');
      });
    }
  }
}
</script>

<style scoped>
.mysql-to-pg-converter {
  padding: 10px;
  height: 100%;
  min-height: calc(100vh - 60px); /* 减去顶部导航栏高度 */
  display: flex;
  flex-direction: column;
}

.converter-container {
  display: flex;
  flex-direction: column;
  gap: 15px;
  width: 100%;
  height: 100%;
  margin: 0;
  flex: 1;
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex: 1;
}

label {
  font-weight: bold;
  color: #333;
}

textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 250px;
  height: 100%;
  flex: 1;
  box-sizing: border-box;
}

textarea:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.2);
}

.button-section {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.convert-button,
.clear-button,
.copy-button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.convert-button {
  background-color: #4CAF50;
  color: white;
}

.convert-button:hover {
  background-color: #45a049;
}

.clear-button {
  background-color: #f44336;
  color: white;
}

.clear-button:hover {
  background-color: #da190b;
}

.copy-button {
  background-color: #2196F3;
  color: white;
  align-self: flex-end;
  padding: 8px 16px;
  font-size: 14px;
}

.copy-button:hover {
  background-color: #0b7dda;
}

.output-section {
  position: relative;
}
</style>