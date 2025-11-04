<template>
  <div class="search-config-manager">
    <h2>搜索配置管理</h2>
    
    <div class="config-list">
      <div class="list-header">
        <h2>搜索配置列表</h2>
        <button @click="showAddConfigModal" class="add-btn">添加配置</button>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading">加载中...</div>
      
      <!-- 错误信息 -->
      <div v-if="error" class="error-message">{{ error }}</div>
      
      <table v-if="!loading && searchConfigs.length > 0" class="config-table">
        <thead>
          <tr>
            <th>数据库</th>
            <th>表</th>
            <th>描述</th>
            <th>搜索字段</th>
            <th>创建时间</th>
            <th>更新时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="config in searchConfigs" :key="config.id">
            <td>{{ config.database }}</td>
            <td>{{ config.table }}</td>
            <td>{{ config.description || '-' }}</td>
            <td>{{ config.searchFields.join(', ') }}</td>
            <td>{{ formatDate(config.createdAt) }}</td>
            <td>{{ formatDate(config.updatedAt) }}</td>
            <td>
              <button @click="editConfig(searchConfigs.indexOf(config))" class="edit-btn">编辑</button>
              <button @click="deleteConfig(searchConfigs.indexOf(config))" class="delete-btn">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      
      <!-- 空状态 -->
      <div v-if="!loading && searchConfigs.length === 0" class="empty-state">
        <p>暂无搜索配置，请点击"添加配置"按钮创建</p>
      </div>
    </div>
    
    <!-- 添加/编辑配置模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ isEdit ? '编辑搜索配置' : '添加搜索配置' }}</h3>
        
        <div class="form-group">
          <label for="database">数据库名</label>
          <select id="database" v-model="formData.database" @change="onDatabaseChange">
            <option value="">请选择数据库</option>
            <option v-for="db in databases" :key="db.name" :value="db.name">
              {{ db.name }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="table">表名</label>
          <select id="table" v-model="formData.table" @change="onTableChange" :disabled="!formData.database">
            <option value="">请选择数据表</option>
            <option v-for="table in tables" :key="table.name" :value="table.name">
              {{ table.desc || table.name }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="description">描述（可选）</label>
          <input type="text" id="description" v-model="formData.description" placeholder="输入配置描述" />
        </div>
        
        <div class="form-group" v-if="tableStructure.length > 0">
          <label>搜索字段</label>
          <div class="field-checkboxes">
            <label v-for="field in tableStructure" :key="field.Field" class="field-checkbox">
              <input 
                type="checkbox" 
                :value="field.Field" 
                v-model="formData.searchFields"
              >
              {{ field.Field }}
            </label>
          </div>
        </div>
        
        <div class="modal-actions">
          <button class="cancel-btn" @click="closeModal">取消</button>
          <button class="save-btn" @click="saveConfig" :disabled="!canSave">{{ isEdit ? '更新' : '保存' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { databaseAPI, searchConfigAPI } from '../api/databaseService';

export default {
  name: 'SearchConfigManager',
  setup() {
    // 状态定义
    const searchConfigs = ref([]);
    const databases = ref([]);
    const tables = ref([]);
    const tableStructure = ref([]);
    const showModal = ref(false);
    const isEdit = ref(false);
    const editIndex = ref(-1);
    const loading = ref(false);
    const error = ref('');
    
    // 表单数据
    const formData = ref({
      database: '',
      table: '',
      searchFields: [],
      description: ''
    });
    
    // 加载搜索配置
    const loadSearchConfigs = async () => {
      loading.value = true;
      error.value = '';
      try {
        console.log('开始加载搜索配置...');
        const startTime = Date.now();
        const response = await searchConfigAPI.getAllSearchConfigs();
        const endTime = Date.now();
        console.log(`搜索配置加载成功，耗时${endTime - startTime}ms，返回${response.length}条记录`);
        
        // 转换后端数据格式为前端使用的格式
        searchConfigs.value = response.map(config => ({
          id: config.id,
          database: config.databaseName,
          table: config.tableName,
          searchFields: JSON.parse(config.searchFields),
          description: config.description,
          createdAt: config.createdAt,
          updatedAt: config.updatedAt
        }));
        console.log('搜索配置转换完成');
      } catch (err) {
        console.error('加载搜索配置失败:', err);
        console.log('错误详情:', {
          message: err.message,
          status: err.response?.status,
          statusText: err.response?.statusText,
          data: err.response?.data,
          config: err.config
        });
        // 更友好的错误消息
        if (err.response?.status === 500) {
          error.value = '服务器内部错误，请联系管理员检查数据库连接和表结构';
        } else if (err.response?.status === 404) {
          error.value = 'API端点不存在，请检查后端服务';
        } else if (err.response?.status === 403) {
          error.value = '没有权限访问该资源';
        } else if (err.message?.includes('Network Error')) {
          error.value = '网络错误，请检查后端服务是否运行';
        } else {
          error.value = `加载搜索配置失败: ${err.message || '未知错误'}`;
        }
        searchConfigs.value = [];
      } finally {
        loading.value = false;
        console.log('搜索配置加载完成');
      }
    };
    
    // 加载数据库列表
    const loadDatabases = async () => {
      try {
        const response = await databaseAPI.getDatabases();
        databases.value = response;
      } catch (error) {
        console.error('加载数据库列表失败:', error);
      }
    };
    
    // 加载数据表列表
    const loadTables = async (databaseName) => {
      try {
        const response = await databaseAPI.getTables(databaseName);
        tables.value = response;
      } catch (error) {
        console.error('加载数据表列表失败:', error);
        tables.value = [];
      }
    };
    
    // 加载表结构
    const loadTableStructure = async (databaseName, tableName) => {
      try {
        const response = await databaseAPI.getTableStructure(databaseName, tableName);
        tableStructure.value = response;
      } catch (error) {
        console.error('加载表结构失败:', error);
        tableStructure.value = [];
      }
    };
    
    // 数据库选择变化
    const onDatabaseChange = () => {
      formData.value.table = '';
      formData.value.searchFields = [];
      tableStructure.value = [];
      
      if (formData.value.database) {
        loadTables(formData.value.database);
      }
    };
    
    // 表选择变化
    const onTableChange = () => {
      formData.value.searchFields = [];
      
      if (formData.value.database && formData.value.table) {
        loadTableStructure(formData.value.database, formData.value.table);
      }
    };
    
    // 显示添加配置模态框
    const showAddConfigModal = () => {
      isEdit.value = false;
      formData.value = {
        database: '',
        table: '',
        searchFields: [],
        description: ''
      };
      tables.value = [];
      tableStructure.value = [];
      showModal.value = true;
    };
    
    // 编辑配置
    const editConfig = (index) => {
      isEdit.value = true;
      editIndex.value = index;
      const config = searchConfigs.value[index];
      formData.value = {
        ...config,
        searchFields: [...config.searchFields] // 深拷贝数组
      };
      
      // 加载相关数据
      loadTables(formData.value.database);
      loadTableStructure(formData.value.database, formData.value.table);
      
      showModal.value = true;
    };
    
    // 删除配置
    const deleteConfig = async (index) => {
      if (confirm('确定要删除这个搜索配置吗？')) {
        const configToDelete = searchConfigs.value[index];
        try {
          await searchConfigAPI.deleteSearchConfig(configToDelete.id);
          // 删除成功后从列表中移除
          searchConfigs.value.splice(index, 1);
        } catch (error) {
          console.error('删除搜索配置失败:', error);
          alert('删除搜索配置失败: ' + (error.message || '未知错误'));
        }
      }
    };
    
    // 保存配置
    const saveConfig = async () => {
      if (!formData.value.database || !formData.value.table || formData.value.searchFields.length === 0) {
        return;
      }
      
      // 准备提交的数据
      const configData = {
        databaseName: formData.value.database,
        tableName: formData.value.table,
        searchFields: JSON.stringify(formData.value.searchFields),
        description: formData.value.description
      };
      
      try {
        let savedConfig;
        if (isEdit.value && formData.value.id) {
          // 编辑现有配置
          savedConfig = await searchConfigAPI.updateSearchConfig(formData.value.id, configData);
          // 更新本地列表
          const index = searchConfigs.value.findIndex(c => c.id === formData.value.id);
          if (index !== -1) {
            searchConfigs.value[index] = {
              id: savedConfig.id,
              database: savedConfig.databaseName,
              table: savedConfig.tableName,
              searchFields: JSON.parse(savedConfig.searchFields),
              description: savedConfig.description,
              createdAt: savedConfig.createdAt,
              updatedAt: savedConfig.updatedAt
            };
          }
        } else {
          // 检查是否已存在相同的数据库和表的配置
          const existingConfig = searchConfigs.value.find(
            config => config.database === formData.value.database && config.table === formData.value.table
          );
          
          if (existingConfig) {
            // 如果存在相同的配置，询问是否覆盖
            if (!confirm('已存在该表的搜索配置，是否覆盖？')) {
              return;
            }
            // 覆盖现有配置
            savedConfig = await searchConfigAPI.updateSearchConfig(existingConfig.id, configData);
            // 更新本地列表
            const index = searchConfigs.value.findIndex(c => c.id === existingConfig.id);
            if (index !== -1) {
              searchConfigs.value[index] = {
                id: savedConfig.id,
                database: savedConfig.databaseName,
                table: savedConfig.tableName,
                searchFields: JSON.parse(savedConfig.searchFields),
                description: savedConfig.description,
                createdAt: savedConfig.createdAt,
                updatedAt: savedConfig.updatedAt
              };
            }
          } else {
            // 添加新配置
            savedConfig = await searchConfigAPI.createSearchConfig(configData);
            searchConfigs.value.push({
              id: savedConfig.id,
              database: savedConfig.databaseName,
              table: savedConfig.tableName,
              searchFields: JSON.parse(savedConfig.searchFields),
              description: savedConfig.description,
              createdAt: savedConfig.createdAt,
              updatedAt: savedConfig.updatedAt
            });
          }
        }
        
        // 关闭模态框
        closeModal();
      } catch (error) {
        console.error('保存搜索配置失败:', error);
        alert('保存搜索配置失败: ' + (error.message || '未知错误'));
      }
    };
    
    // 关闭模态框
      const closeModal = () => {
        showModal.value = false;
        isEdit.value = false;
        editIndex.value = -1;
      };
      
      // 计算是否可以保存
      const canSave = computed(() => {
        return formData.value.database && formData.value.table && formData.value.searchFields.length > 0;
      });
      
      // 计算选中的字段数量
      const selectedFieldCount = computed(() => {
        return formData.value.searchFields.length;
      });
      
      // 格式化日期
      const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        });
      };
      
      // 组件挂载时加载数据
      onMounted(() => {
        loadSearchConfigs();
        loadDatabases();
      });
      
      return {
        searchConfigs,
        databases,
        tables,
        tableStructure,
        showModal,
        isEdit,
        editIndex,
        formData,
        canSave,
        selectedFieldCount,
        loading,
        error,
        formatDate,
        onDatabaseChange,
        onTableChange,
        showAddConfigModal,
        editConfig,
        deleteConfig,
        saveConfig,
        closeModal
      };
    }
  };
</script>

<style scoped>
/* 搜索配置管理器容器样式 */
.search-config-manager {
  padding: 20px;
}

.search-config-manager h2 {
  margin-bottom: 20px;
  color: #333;
}

/* 配置列表样式 */
.config-list {
  margin-bottom: 20px;
}

/* 列表头部样式 */
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.add-btn {
  padding: 0.5rem 1rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn:hover {
  background-color: #45a049;
}

/* 加载状态样式 */
.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}

/* 错误信息样式 */
.error-message {
  background-color: #ffebee;
  color: #c62828;
  padding: 1rem;
  border-radius: 4px;
  margin-bottom: 1rem;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

/* 表格样式 */
.config-table {
  width: 100%;
  border-collapse: collapse;
}

.config-table th,
.config-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.config-table th {
  background-color: #f2f2f2;
}

/* 按钮样式 */
.edit-btn {
  background-color: #2196F3;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  cursor: pointer;
  margin-right: 0.5rem;
}

.edit-btn:hover {
  background-color: #0b7dda;
}

.delete-btn {
  background-color: #f44336;
  color: white;
  border: none;
  padding: 0.25rem 0.5rem;
  border-radius: 3px;
  cursor: pointer;
}

.delete-btn:hover {
  background-color: #da190b;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 30px;
  border-radius: 5px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-content h3 {
  margin-top: 0;
  color: #333;
}

/* 表单样式 */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: bold;
}

.form-group select,
.form-group input[type="text"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

/* 字段选择框样式 */
.field-checkboxes {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
}

.field-checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
  cursor: pointer;
  padding: 5px;
}

.field-checkbox:hover {
  background-color: #f5f5f5;
}

.field-checkbox input[type="checkbox"] {
  cursor: pointer;
}

/* 模态框操作按钮样式 */
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.cancel-btn,
.save-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.cancel-btn {
  background-color: #ffffff;
  color: #666666;
  border: 1px solid #ddd;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background-color: #f8f9fa;
  border-color: #bbb;
  color: #333333;
}

.save-btn {
  background-color: #4CAF50;
  color: white;
}

.save-btn:hover:not(:disabled) {
  background-color: #45a049;
}

.save-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>