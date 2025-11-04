<template>
  <div class="table-data-manager">
    <h2>表数据管理</h2>
    

    
    <!-- 数据库和表选择器 -->
    <a-form layout="inline" :style="{ marginBottom: '16px' }">
      <a-form-item label="数据库">
        <a-select 
          v-model:value="selectedDatabase"
          @change="handleDatabaseChange"
          placeholder="请选择数据库"
          :loading="loadingDatabases"
          :options="databases.map(db => ({ label: db, value: db }))"
          style="width: 250px;"
        />
      </a-form-item>
      
      <a-form-item label="表">
        <a-select 
          v-model:value="selectedTable"
          @change="handleTableChange"
          placeholder="请选择表"
          :loading="loadingTables"
          :disabled="!selectedDatabase"
          :options="tables.map(table => ({ label: table, value: table }))"
          style="width: 250px;"
        />
      </a-form-item>
    </a-form>
    

    
    <!-- 错误信息显示 -->
    <a-alert 
      v-if="error || searchError"
      :message="error || searchError"
      type="error" 
      show-icon 
      :style="{ marginBottom: '16px' }"
    />
    
    <!-- 搜索区域 -->
    <a-card v-if="searchConfigs.length > 0" :style="{ marginBottom: '16px' }">
      <template #title>
        搜索条件
      </template>
      <a-form layout="inline" :style="{ marginBottom: '16px' }">
        <template v-for="field in allSearchFields" :key="field">
          <a-form-item :label="field">
            <a-input 
              v-model:value="searchQuery[field]"
              placeholder="请输入搜索关键词"
              style="width: 200px"
            />
          </a-form-item>
        </template>
      </a-form>
      <div style="display: flex; gap: 8px; justify-content: flex-end;">
        <a-button type="primary" @click="handleSearch" :loading="searchLoading">
          搜索
        </a-button>
        <a-button @click="handleResetSearch">重置</a-button>
      </div>
    </a-card>
    
    <!-- 添加记录按钮 -->
    <div v-if="selectedDatabase && selectedTable" style="margin-bottom: 16px; display: flex; gap: 10px;">
      <a-button type="primary" @click="showAddForm">
        <template #icon>
          <plus-outlined />
        </template>
        添加记录
      </a-button>
    </div>
    
    <!-- 表数据显示 -->
    <a-spin :spinning="loading">
      <a-table 
        v-if="tableStructure.length > 0 && tableData.length > 0" 
        :columns="columns" 
        :data-source="tableData" 
        :pagination="false"
        :row-key="getRecordKey"
        :scroll="{ x: 'max-content' }"
        size="middle"
        :style="{ minWidth: '100%' }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space size="small">
              <a-button type="link" @click="() => handleEdit(record)">编辑</a-button>
              <a-button type="link" danger @click="() => handleDelete(record)">删除</a-button>
            </a-space>
          </template>
          <template v-else>
            {{ formatValue(record[column.dataIndex], column.field) }}
          </template>
        </template>
      </a-table>
      
      <!-- 无数据状态 -->
      <div v-else-if="!loading" class="no-data">
        <a-empty description="暂无数据" />
      </div>
    </a-spin>
    
    <!-- 分页 -->
    <a-pagination 
      v-if="totalRecords > pageSize"
      :total="totalRecords" 
      :current="currentPage"
      :pageSize="pageSize"
      @change="handlePageChange"
      @showSizeChange="handleShowSizeChange"
      showSizeChanger
      :showTotal="showTotalFunction"
      :style="{ marginTop: '16px', textAlign: 'center' }"
    />
    
    <!-- 编辑记录模态框，使用Ant Design Vue的Modal组件 -->
    <a-modal
      v-model:visible="showEditModal"
      title="编辑记录"
      @ok="saveEdit"
      @cancel="cancelEdit"
      @afterClose="afterEditModalClose"
      centered
      width="600px"
      :maskClosable="false"
    >
      <form @submit.prevent="saveEdit">
        <div 
          v-for="field in tableStructure" 
          :key="field.Field"
          class="form-group"
          style="margin-bottom: 16px"
        >
          <label :for="field.Field">{{ field.Field }}
            <span v-if="field.Null === 'NO' && !isPrimaryKey(field)" class="required-mark">*</span>
            <span v-if="isPrimaryKey(field)" class="readonly-mark">(只读)</span>
          </label>
          <a-input
            :id="field.Field"
            v-model:value="editForm[field.Field]"
            :type="getFieldType(field.Type)"
            :readonly="isPrimaryKey(field)"
            :required="field.Null === 'NO' && !isPrimaryKey(field)"
            :placeholder="`请输入${field.Field}`"
            style="width: 100%"
          />
        </div>
      </form>
    </a-modal>
    
    <!-- 添加记录模态框，使用Ant Design Vue的Modal组件 -->
    <a-modal
      v-model:visible="showAddModal"
      title="添加记录"
      @ok="saveAdd"
      @cancel="cancelAdd"
      @afterClose="afterAddModalClose"
      centered
      width="600px"
      :maskClosable="false"
    >
      <form @submit.prevent="saveAdd">
        <div 
          v-for="field in tableStructure" 
          :key="field.Field"
          v-if="!isPrimaryKey(field) || !(field.Extra && field.Extra.includes('auto_increment'))"
          class="form-group"
          style="margin-bottom: 16px"
        >
          <label :for="field.Field">{{ field.Field }}
            <span v-if="field.Null === 'NO'" class="required-mark">*</span>
          </label>
          <a-input
            :id="field.Field"
            v-model:value="addForm[field.Field]"
            :type="getFieldType(field.Type)"
            :required="field.Null === 'NO'"
            :placeholder="`请输入${field.Field}`"
            style="width: 100%"
          />
        </div>
      </form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { databaseAPI, searchConfigAPI } from '../api/databaseService';
import { 
  Form, 
  Select, 
  Input, 
  Button, 
  Table, 
  Modal, 
  Spin, 
  Alert, 
  Pagination, 
  Space, 
  Empty,
  Card,
  Row,
  Col,
  message
} from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';

// 定义组件props（保持兼容性）
const props = defineProps({
  databaseName: {
    type: String,
    required: false,
    default: null
  },
  tableName: {
    type: String,
    required: false,
    default: null
  }
});

// 获取路由和路由器实例
const route = useRoute();
const router = useRouter();

// 定义组件事件
const emit = defineEmits(['dataUpdated']);

// 表数据状态
const tableData = ref([]);
const tableStructure = ref([]);
const totalRecords = ref(0);
const currentPage = ref(1);
const pageSize = ref(100);
const loading = ref(false);
const error = ref('');

// 数据库和表的选择状态
const databases = ref([]);
const tables = ref([]);
const selectedDatabase = ref('');
const selectedTable = ref('');
const loadingDatabases = ref(false);
const loadingTables = ref(false);

// 搜索相关状态
const searchConfigs = ref([]);
const searchQuery = ref({});
const searchLoading = ref(false);
const searchError = ref('');
const allSearchFields = ref([]);

// 编辑相关状态
const editingRow = ref(null);
const editForm = ref({});
const showEditModal = ref(false);
const showAddModal = ref(false);
const addForm = ref({});

// 检查是否为主键
const isPrimaryKey = (field) => {
  return field && field.isPrimaryKey === true;
};

// 获取字段类型
const getFieldType = (type) => {
  const lowerType = type.toLowerCase();
  if (lowerType.includes('int') || lowerType.includes('double') || 
      lowerType.includes('float') || lowerType.includes('decimal')) {
    return 'number';
  } else if (lowerType.includes('date') || lowerType.includes('time')) {
    return 'datetime-local';
  } else if (lowerType.includes('boolean')) {
    return 'checkbox';
  } else {
    return 'text';
  }
};

// 计算表格列
const columns = computed(() => {
  const dataColumns = tableStructure.value.map(field => ({
    title: field.Field,
    dataIndex: field.Field,
    key: field.Field,
    field: field
  }));
  
  const actionColumn = {
    title: '操作',
    key: 'action',
    width: 120,
    render(_, record) {
      // 使用数组形式而不是JSX
      return {
        type: 'a-space',
        size: 'small',
        children: [
          {
            type: 'a-button',
            props: {
              type: 'link'
            },
            on: {
              click: () => handleEdit(record)
            },
            children: '编辑'
          },
          {
            type: 'a-button',
            props: {
              type: 'link',
              danger: true
            },
            on: {
              click: () => handleDelete(record)
            },
            children: '删除'
          }
        ]
      };
    }
  };
  
  return [...dataColumns, actionColumn];
});

// 加载数据库列表
const loadDatabases = async () => {
  loadingDatabases.value = true;
  try {
    const response = await databaseAPI.getDatabases();
    console.log('数据库列表响应:', response);
    
    // 处理响应数据格式，确保databases是字符串数组
    if (Array.isArray(response)) {
      // 检查每个元素是否为对象，如果是则取name属性
      if (response.length > 0 && typeof response[0] === 'object' && response[0] !== null) {
        databases.value = response.map(db => db.name);
      } else {
        databases.value = response;
      }
    } else {
      databases.value = [];
    }
    
    console.log('处理后的数据库列表:', databases.value);
    
    // 如果有传入的数据库名或当前未选择数据库，则选择第一个
    if ((props.databaseName && !selectedDatabase.value) || (!selectedDatabase.value && databases.value.length > 0)) {
      selectedDatabase.value = props.databaseName || databases.value[0];
      await loadTables();
    }
  } catch (err) {
    console.error('加载数据库列表失败:', err);
    error.value = `加载数据库列表失败: ${err.message || '未知错误'}`;
  } finally {
    loadingDatabases.value = false;
  }
};

// 加载指定数据库的表列表
const loadTables = async () => {
  if (!selectedDatabase.value) {
    tables.value = [];
    selectedTable.value = '';
    return;
  }
  
  loadingTables.value = true;
  try {
    console.log('加载表列表，数据库名:', selectedDatabase.value);
    const response = await databaseAPI.getTables(selectedDatabase.value);
    console.log('表列表响应:', response);
    
    // 处理响应数据格式，确保tables是字符串数组
    if (Array.isArray(response)) {
      // 检查每个元素是否为对象，如果是则取name属性
      if (response.length > 0 && typeof response[0] === 'object' && response[0] !== null) {
        tables.value = response.map(table => table.name);
      } else {
        tables.value = response;
      }
    } else {
      tables.value = [];
    }
    
    console.log('处理后的表列表:', tables.value);
    
    // 保存当前表名以便比较
    const previousTable = selectedTable.value;
    
    // 如果有传入的表名或当前未选择表，则选择第一个
    if ((props.tableName && !selectedTable.value) || (!selectedTable.value && tables.value.length > 0)) {
      selectedTable.value = props.tableName || tables.value[0];
    }
    
    // 如果表名发生了变化，触发表选择变化处理
    if (selectedTable.value && selectedTable.value !== previousTable) {
      await handleTableChange(selectedTable.value);
    }
  } catch (err) {
    console.error('加载表列表失败:', err);
    error.value = `加载表列表失败: ${err.message || '未知错误'}`;
  } finally {
    loadingTables.value = false;
  }
};

// 处理数据库选择变化
const handleDatabaseChange = async (database) => {
  selectedDatabase.value = database;
  selectedTable.value = '';
  
  // 清除之前的数据
  tableData.value = [];
  tableStructure.value = [];
  searchConfigs.value = [];
  searchQuery.value = {};
  
  await loadTables();
  
  // 注意：不再在这里额外调用loadSearchConfigs和loadTableData
  // 因为loadTables函数内部会在设置selectedTable后自动调用handleTableChange
};

// 处理表选择变化
const handleTableChange = async (table) => {
  selectedTable.value = table;
  
  // 清除之前的数据
  tableData.value = [];
  tableStructure.value = [];
  searchConfigs.value = [];
  searchQuery.value = {};
  
  // 加载新表的数据
  await loadSearchConfigs();
  await loadTableData();
};

// 加载搜索配置
const loadSearchConfigs = async () => {
  searchLoading.value = true;
  searchError.value = '';
  try {
    const currentDb = selectedDatabase.value || props.databaseName;
    const currentTable = selectedTable.value || props.tableName;
    
    if (currentDb && currentTable) {
      const response = await searchConfigAPI.getSearchConfigsByDatabaseAndTable(
        currentDb, 
        currentTable
      );
      // 转换搜索配置格式，确保searchFields是数组
      searchConfigs.value = response.map(config => ({
        ...config,
        searchFields: typeof config.searchFields === 'string' 
          ? JSON.parse(config.searchFields) 
          : config.searchFields
      }));
      
      // 合并所有搜索字段到一个扁平数组
      allSearchFields.value = [];
      searchConfigs.value.forEach(config => {
        allSearchFields.value.push(...config.searchFields);
      });
      
      // 初始化搜索查询对象
      searchQuery.value = {};
      allSearchFields.value.forEach(field => {
        searchQuery.value[field] = '';
      });
    } else {
      searchConfigs.value = [];
      searchQuery.value = {};
    }
  } catch (err) {
    console.error('加载搜索配置失败:', err);
    searchError.value = `加载搜索配置失败: ${err.message || '未知错误'}`;
    searchConfigs.value = [];
  } finally {
    searchLoading.value = false;
  }
};

// 加载表数据（支持搜索）
const loadTableData = async (page = 1) => {
  loading.value = true;
  error.value = '';
  try {
    const currentDb = selectedDatabase.value || props.databaseName;
    const currentTable = selectedTable.value || props.tableName;
    
    if (!currentDb || !currentTable) {
      console.warn('未选择数据库或表，无法加载数据');
      tableData.value = [];
      tableStructure.value = [];
      totalRecords.value = 0;
      return;
    }
    
    console.log('开始加载表数据:', {
      databaseName: currentDb,
      tableName: currentTable,
      page: page,
      pageSize: pageSize.value,
      offset: (page - 1) * pageSize.value,
      searchQuery: searchQuery.value
    });
    
    // 先加载表结构
    const structureResponse = await databaseAPI.getTableStructure(currentDb, currentTable);
    // 处理表结构，确保每个字段都有isPrimaryKey属性
    tableStructure.value = structureResponse.map(field => ({
      ...field,
      // 确保isPrimaryKey属性存在，如果不存在则默认为false
      isPrimaryKey: field.isPrimaryKey === true
    }));
    console.log('表结构加载成功:', tableStructure.value);
    
    // 构建搜索条件参数 - 修改为使用page和pageSize参数而非offset和limit，以匹配后端API
    const params = {
      page: page,
      pageSize: pageSize.value
    };
    
    // 添加非空搜索条件
    Object.keys(searchQuery.value).forEach(key => {
      if (searchQuery.value[key]) {
        // 使用like匹配模式
        params[`search.${key}`] = `%${searchQuery.value[key]}%`;
      }
    });
    
    // 再加载表数据 - 只传递数据库名、表名和params参数，不再单独传递offset和limit
    const dataResponse = await databaseAPI.getTableData(
      currentDb, 
      currentTable, 
      page, 
      pageSize.value,
      params
    );
    tableData.value = dataResponse.data || [];
    totalRecords.value = dataResponse.total || tableData.value.length;
    currentPage.value = page;
    console.log('表数据加载成功:', { total: totalRecords.value, count: tableData.value.length });
  } catch (err) {
    console.error('加载表数据失败:', {
      error: err,
      requestConfig: err.config,
      response: err.response,
      databaseName: props.databaseName,
      tableName: props.tableName
    });
    error.value = `加载表数据失败: ${err.message || '未知错误'}`;
    // 在开发环境下，显示更详细的错误信息
    if (import.meta.env.DEV) {
      error.value += `\n请尝试访问: http://localhost:5173/test-database-api.html 测试API连接`;
    }
  } finally {
    loading.value = false;
  }
};

// 执行搜索
const handleSearch = () => {
  loadTableData(1); // 搜索时重置到第一页
};

// 重置搜索
const handleResetSearch = () => {
  // 重置搜索查询对象
  searchConfigs.value.forEach(config => {
    config.searchFields.forEach(field => {
      searchQuery.value[field] = '';
    });
  });
  loadTableData(1); // 重置后重新加载数据
};

// 编辑按钮点击处理
const handleEdit = (record) => {
  console.log('handleEdit被调用，记录:', record);
  // 确保editingRow和editForm是响应式更新的
  editingRow.value = { ...record };
  editForm.value = { ...record };
  
  // 直接显示模态框，Ant Design Vue的Modal组件会处理好DOM更新
  showEditModal.value = true;
  console.log('设置showEditModal为true后的值:', showEditModal.value);
};

// 保存编辑
const saveEdit = async () => {
  console.log('保存编辑，值:', editForm.value);
  if (!selectedDatabase.value || !selectedTable.value) {
    message.error('请先选择数据库和表');
    return;
  }
  try {
    await databaseAPI.updateRecord(selectedDatabase.value, selectedTable.value, editingRow.value.id, editForm.value);
    message.success('更新成功');
    showEditModal.value = false;
    await loadTableData();
  } catch (error) {
    message.error('更新失败');
    console.error('更新失败:', error);
  }
};

// 取消编辑
const cancelEdit = () => {
  console.log('取消编辑');
  // 直接设置visible为false，由afterClose事件处理数据清空
  showEditModal.value = false;
};

// 编辑模态框完全关闭后的回调
const afterEditModalClose = () => {
  console.log('编辑模态框完全关闭');
  // 模态框完全关闭后再清空数据，避免组件更新错误
  editingRow.value = null;
  editForm.value = {};
};

// 分页的总记录数显示函数
const showTotalFunction = (total) => {
  return `共 ${total} 条记录`;
};

// 处理删除记录
const handleDelete = async (row) => {
  try {
    console.log('删除按钮被点击，行数据:', row);
    
    await Modal.confirm({
      title: '确认删除',
      content: '确定要删除这条记录吗？',
      async onOk() {
        // 获取主键值
        const primaryKey = tableStructure.value.find(field => field.isPrimaryKey);
        if (!primaryKey) {
          throw new Error('无法找到表的主键字段');
        }
        
        const primaryKeyValue = row[primaryKey.Field];
        
        // 使用当前选中的数据库和表名，而不是props中的值
        const currentDb = selectedDatabase.value || props.databaseName;
        const currentTable = selectedTable.value || props.tableName;
        
        console.log('准备调用API删除记录:', {
          database: currentDb,
          table: currentTable,
          primaryKey: primaryKeyValue
        });
        
        await databaseAPI.deleteRecord(
          currentDb, 
          currentTable, 
          primaryKeyValue
        );
        
        // 如果删除的是当前页的最后一条记录且不是第一页，则返回上一页
        if (tableData.value.length === 1 && currentPage.value > 1) {
          loadTableData(currentPage.value - 1);
        } else {
          loadTableData(currentPage.value);
        }
        emit('dataUpdated');
      }
    });
  } catch (err) {
    console.error('删除记录失败:', err);
    error.value = '删除记录失败: ' + (err.message || '未知错误');
  }
};

// 显示添加记录模态框
const showAddForm = () => {
  console.log('showAddForm被调用');
  // 获取表结构，初始化添加表单
  addForm.value = {};
  tableStructure.value.forEach(field => {
    // 添加模式下，自增主键不显示
    if (!field.isPrimaryKey || !(field.Extra && field.Extra.includes('auto_increment'))) {
      addForm.value[field.Field] = '';
    }
  });
  showAddModal.value = true;
  console.log('设置showAddModal为true后的值:', showAddModal.value);
};

// 保存添加记录
const saveAdd = async () => {
  try {
    // 构建提交数据
    const submitData = { ...addForm.value };
    
    // 处理特殊类型的字段
    tableStructure.value.forEach(field => {
      const fieldName = field.Field;
      const fieldType = field.Type.toLowerCase();
      
      // 数字类型转换
      if ((fieldType.includes('int') || fieldType.includes('double') || 
           fieldType.includes('float') || fieldType.includes('decimal')) && 
          submitData[fieldName] !== '' && submitData[fieldName] !== null) {
        submitData[fieldName] = Number(submitData[fieldName]);
      }
      // 空字符串转换为null
      if (submitData[fieldName] === '') {
        submitData[fieldName] = null;
      }
    });
    
    await databaseAPI.createRecord(props.databaseName, props.tableName, submitData);
    showAddModal.value = false;
    loadTableData(currentPage.value);
    emit('dataUpdated');
  } catch (err) {
    error.value = '添加记录失败: ' + (err.message || '未知错误');
    console.error('添加记录失败:', err);
  }
};

// 取消添加
const cancelAdd = () => {
  showAddModal.value = false;
  // 数据清空由afterClose事件处理
};

// 添加模态框完全关闭后的回调
const afterAddModalClose = () => {
  console.log('添加模态框完全关闭');
  // 模态框完全关闭后再清空数据，避免组件更新错误
  addForm.value = {};
};



// 获取记录的主键值
const getRecordKey = (record) => {
  const primaryKey = tableStructure.value.find(field => field.isPrimaryKey);
  if (primaryKey) {
    return record[primaryKey.Field];
  }
  // 如果没有主键，使用id字段
  if (record.id !== undefined) {
    return record.id;
  }
  // 否则使用索引
  return tableData.value.indexOf(record);
};

// 格式化显示值
const formatValue = (value, column) => {
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
  // 处理JSON对象
  if (typeof value === 'object' && value !== null) {
    try {
      return JSON.stringify(value);
    } catch (e) {
      return String(value);
    }
  }
  return value;
};

// 分页处理
const handlePageChange = (page) => {
  loadTableData(page);
};

const handleShowSizeChange = (current, size) => {
  pageSize.value = size;
  loadTableData(1);
};

// 组件挂载时加载数据
onMounted(() => {
  // 首先尝试从路由参数中获取数据库和表名
  const routeDbName = route.query.databaseName;
  const routeTableName = route.query.tableName;
  
  // 设置初始选中的数据库和表
  if (routeDbName) {
    selectedDatabase.value = routeDbName;
  }
  if (routeTableName) {
    selectedTable.value = routeTableName;
  }
  
  // 加载数据库列表
  loadDatabases();
  
  // 如果从路由参数中获取了数据库和表名，直接加载数据
  if (routeDbName && routeTableName) {
    loadSearchConfigs().then(() => {
      loadTableData();
    });
  }
});

// 监听路由参数变化，重新加载数据
watch(() => [route.query.databaseName, route.query.tableName], ([newDbName, newTableName]) => {
  if (newDbName && newTableName) {
    console.log('路由参数变化，准备加载数据:', { newDbName, newTableName });
    // 设置选中的数据库和表
    selectedDatabase.value = newDbName;
    selectedTable.value = newTableName;
    
    // 直接加载数据
    loadSearchConfigs().then(() => {
      loadTableData();
    }).catch(error => {
      console.error('加载数据失败:', error);
    });
  }
});

// 保留props监听以保持兼容性，但优先使用路由参数
watch(() => [props.databaseName, props.tableName], ([newDbName, newTableName]) => {
  // 只有在没有路由参数的情况下才响应props变化
  if (newDbName && newTableName && !route.query.databaseName) {
    console.log('Props变化，准备加载数据:', { newDbName, newTableName });
    // 设置选中的数据库，但不直接设置表名，让loadTables来处理
    selectedDatabase.value = newDbName;
    
    // 加载表列表，loadTables函数内部会根据props.tableName设置selectedTable并触发数据加载
    loadTables().then(() => {
      // 额外检查：如果表名设置成功但数据为空，强制重新加载一次
      if (selectedTable.value === newTableName && tableData.value.length === 0) {
        console.log('表数据为空，强制重新加载一次');
        loadTableData();
      }
    }).catch(error => {
      console.error('加载数据失败:', error);
      // 失败时强制尝试直接加载数据
      try {
        loadSearchConfigs().then(() => {
          loadTableData();
        });
      } catch (err) {
        console.error('强制加载也失败:', err);
      }
    });
  }
});
</script>

<style scoped>
.table-data-manager {
  background-color: #f0f2f5;
  padding: 20px;
  min-height: 100vh;
}

h2 {
  margin-bottom: 20px;
}

.no-data {
  text-align: center;
  padding: 50px;
  color: #999;
}

/* 解决antd的下拉框显示问题 */
:deep(.ant-select-dropdown) {
  z-index: 1060 !important;
}

/* 编辑模态框样式 */
.table-editor-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000; /* 提高z-index确保模态框在最上层 */
  padding: 20px;
  transition: opacity 0.3s ease;
}

.table-editor-overlay[style*="display: none"] {
  opacity: 0;
  pointer-events: none;
}

.table-editor-dialog {
  background-color: white;
  border-radius: 4px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.dialog-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: #666;
  transition: all 0.3s;
}

.close-btn:hover {
  background-color: #f5f5f5;
  color: #333;
}

.dialog-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.record-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 500;
  color: #333;
  display: flex;
  align-items: center;
  gap: 4px;
}

.required-mark {
  color: #ff4d4f;
  font-size: 14px;
}

.readonly-mark {
  color: #999;
  font-size: 12px;
  font-weight: normal;
}

.form-group input {
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #40a9ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-group input:read-only {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn, .save-btn {
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  border: 1px solid transparent;
}

.cancel-btn {
  border-color: #d9d9d9;
  background-color: white;
  color: #333;
}

.cancel-btn:hover {
  border-color: #40a9ff;
  color: #40a9ff;
}

.save-btn {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

.save-btn:hover {
  background-color: #40a9ff;
  border-color: #40a9ff;
}
</style>