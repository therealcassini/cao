<template>
  <div class="table-data">
    <div class="table-header">
      <button class="add-btn" @click="handleAddRecord">添加记录</button>
      <button class="refresh-btn" @click="handleRefresh">刷新</button>
    </div>
    
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>操作</th>
            <th v-for="column in getColumns()" :key="column.Field">
              {{ column.Field }}
              <span v-if="column.isPrimaryKey" class="primary-key-mark">*</span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in tableData.data" :key="getRecordKey(record)">
            <td class="action-buttons">
              <button class="edit-btn" @click="handleEdit(record)">编辑</button>
              <button class="delete-btn" @click="handleDelete(record)">删除</button>
            </td>
            <td v-for="column in getColumns()" :key="column.Field">
              {{ formatValue(record[column.Field]) }}
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 空数据提示 -->
      <div v-if="tableData.data.length === 0" class="empty-data">
        暂无数据
      </div>
    </div>
    
    <!-- 分页信息 -->
    <div class="pagination-info" v-if="tableData.total > 0">
      共 {{ tableData.total }} 条记录，每页显示 100 条
    </div>
  </div>
</template>

<script>
export default {
  name: 'TableData',
  props: {
    tableData: {
      type: Object,
      default: () => ({ data: [], total: 0, page: 1, pageSize: 100 })
    },
    tableStructure: {
      type: Array,
      default: () => []
    },
    tableName: {
      type: String,
      default: ''
    }
  },
  emits: ['delete-record', 'edit-record', 'add-record', 'refresh-data'],
  methods: {
    // 获取列信息
    getColumns() {
      return this.tableStructure;
    },
    
    // 获取记录的主键值
    getRecordKey(record) {
      const primaryKey = this.tableStructure.find(field => field.isPrimaryKey);
      if (primaryKey) {
        return record[primaryKey.Field];
      }
      // 如果没有主键，使用id字段
      if (record.id !== undefined) {
        return record.id;
      }
      // 否则使用索引
      return this.tableData.data.indexOf(record);
    },
    
    // 格式化显示值
    formatValue(value) {
      if (value === null || value === undefined) {
        return '-';
      }
      // 处理日期类型
      if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}.\d{3}Z$/.test(value)) {
        return new Date(value).toLocaleString('zh-CN');
      }
      // 处理大文本
      if (typeof value === 'string' && value.length > 100) {
        return value.substring(0, 100) + '...';
      }
      return value;
    },
    
    // 处理编辑
    handleEdit(record) {
      this.$emit('edit-record', record);
    },
    
    // 处理删除
    handleDelete(record) {
      const key = this.getRecordKey(record);
      this.$emit('delete-record', this.tableName, key);
    },
    
    // 处理添加
    handleAddRecord() {
      this.$emit('add-record');
    },
    
    // 处理刷新
    handleRefresh() {
      this.$emit('refresh-data', this.tableName);
    }
  }
};
</script>

<style scoped>
.table-header {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.add-btn, .refresh-btn {
  background-color: #4CAF50;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background-color 0.3s ease;
}

.add-btn:hover {
  background-color: #45a049;
}

.refresh-btn {
  background-color: #9E9E9E;
}

.refresh-btn:hover {
  background-color: #757575;
}

.table-container {
  background-color: #fff;
  border-radius: 5px;
  overflow-x: auto;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th, .data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.data-table th {
  background-color: #f2f2f2;
  font-weight: bold;
  color: #333;
  position: sticky;
  top: 0;
  z-index: 1;
}

.data-table tr:hover {
  background-color: #f5f5f5;
}

.action-buttons {
  white-space: nowrap;
}

.edit-btn, .delete-btn {
  padding: 5px 10px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background-color 0.3s ease;
  margin-right: 5px;
}

.edit-btn {
  background-color: #2196F3;
  color: white;
}

.edit-btn:hover {
  background-color: #0b7dda;
}

.delete-btn {
  background-color: #f44336;
  color: white;
}

.delete-btn:hover {
  background-color: #da190b;
}

.primary-key-mark {
  color: #f44336;
  font-weight: bold;
}

.empty-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination-info {
  margin-top: 15px;
  text-align: right;
  color: #666;
  font-size: 0.9rem;
}
</style>