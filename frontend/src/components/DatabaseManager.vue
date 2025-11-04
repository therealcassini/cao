<template>
  <div class="database-manager">
    <h1>数据库管理系统</h1>
    
    <!-- 数据库列表 -->
    <div class="section">
      <h2>数据库列表</h2>
      <DatabaseList 
        :databases="databases" 
        @select-database="selectDatabase"
        @load-tables="loadTables"
      />
    </div>
    
    <!-- 数据表列表 -->
    <div v-if="selectedDatabase" class="section">
      <h2>{{ selectedDatabase }} - 数据表列表</h2>
      <TableList 
        :tables="tables" 
        @select-table="selectTable"
        @load-data="loadTableData"
      />
    </div>
    
    <!-- 提示信息：点击表名查看数据 -->
    <div v-if="selectedTable" class="section">
      <h2>{{ selectedTable }} - 数据列表</h2>
      <div class="data-tip">
        <p>点击上方的"表数据管理"标签页查看和管理表数据</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import DatabaseList from './DatabaseList.vue';
import TableList from './TableList.vue';
import { databaseAPI } from '../api/databaseService';

export default {
  name: 'DatabaseManager',
  components: {
    DatabaseList,
    TableList
  },
    setup() {
      // 状态定义
      const router = useRouter();
      // 注入切换标签页的方法
      const switchTab = inject('switchTab');
      const databases = ref([]);
      const selectedDatabase = ref('');
      const tables = ref([]);
      const selectedTable = ref('');
      const tableStructure = ref([]);

      // 加载数据库列表
      const loadDatabases = async () => {
        try {
          console.log('开始加载数据库列表');
          const response = await databaseAPI.getDatabases();
          databases.value = response;
          console.log('数据库列表加载成功，共', response.length, '个数据库');
        } catch (error) {
          console.error('加载数据库列表失败:', {
            error: error,
            requestConfig: error.config,
            response: error.response
          });
          alert('加载数据库列表失败，请检查后端服务是否正常运行。\n\n错误详情:\n' + (error.message || '未知错误'));
        }
      };

      // 选择数据库
      const selectDatabase = (database) => {
        selectedDatabase.value = database.name;
        selectedTable.value = '';
        tables.value = [];
        tableStructure.value = [];
      };

      // 加载数据表列表
      const loadTables = async (database) => {
        try {
          console.log('开始加载数据表列表:', { database });
          const response = await databaseAPI.getTables(database);
          tables.value = response;
          console.log('数据表列表加载成功，共', response.length, '个数据表');
        } catch (error) {
          console.error('加载数据表列表失败:', {
            error: error,
            requestConfig: error.config,
            response: error.response,
            database: database
          });
          alert('加载数据表列表失败:\n\n错误详情:\n' + (error.message || '未知错误'));
        }
      };

      // 选择数据表
      const selectTable = (table) => {
        selectedTable.value = table.name;
        // 选择表后自动切换到表数据管理标签页
        switchTab('table-data');
      };

      // 加载数据表数据
      const loadTableData = async (table) => {
        try {
          console.log('开始加载数据表结构:', { table, database: selectedDatabase.value });
          // 先加载表结构
          const structureResponse = await databaseAPI.getTableStructure(selectedDatabase.value, table);
          tableStructure.value = structureResponse;
          console.log('表结构加载成功:', tableStructure.value);

          // 使用router跳转到表数据管理页面，并传递参数
          router.push({
            name: 'TableManager',
            query: {
              databaseName: selectedDatabase.value,
              tableName: table
            }
          });
        } catch (error) {
          console.error('加载数据表结构失败:', {
            error: error,
            requestConfig: error.config,
            response: error.response,
            table: table,
            database: selectedDatabase.value
          });
          alert('加载数据表结构失败:\n\n请尝试以下操作:\n1. 确认后端服务正在运行\n2. 检查API路径是否正确\n3. 访问 http://localhost:5173/test-database-api.html 测试API连接\n\n错误详情:\n' + (error.message || '未知错误'));
        }
      };

      // 组件挂载时加载数据库列表
      onMounted(() => {
        loadDatabases();
      });

      return {
        databases,
        selectedDatabase,
        tables,
        selectedTable,
        tableStructure,
        selectDatabase,
        loadTables,
        selectTable,
        loadTableData,
        router
      };
    }
};
</script>

<style scoped>
.database-manager {
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  color: #333;
  margin-bottom: 30px;
}

h2 {
  color: #555;
  margin-top: 30px;
  margin-bottom: 20px;
  font-size: 1.5rem;
}

.section {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 5px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.data-tip {
  padding: 20px;
  text-align: center;
  background-color: #fff;
  border: 1px dashed #ccc;
  border-radius: 5px;
  color: #666;
}

.data-tip p {
  margin: 0;
  font-size: 14px;
}
</style>