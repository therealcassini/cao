import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api/database',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 可以在这里添加token等认证信息
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    console.log('API请求成功:', response.config.url, '状态码:', response.status);
    return response.data;
  },
  error => {
    console.error('API请求错误:', error);
    console.log('请求URL:', error.config?.url);
    console.log('请求配置:', JSON.stringify(error.config, null, 2));
    console.log('错误状态:', error.response?.status);
    console.log('错误响应:', error.response?.data);
    return Promise.reject(error);
  }
);

// 创建搜索配置API实例
const searchConfigAPIInstance = axios.create({
  baseURL: '/api/search-configs',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 为搜索配置API添加响应拦截器
searchConfigAPIInstance.interceptors.response.use(
  response => {
    console.log('搜索配置API请求成功:', response.config.url, '状态码:', response.status);
    return response.data;
  },
  error => {
    console.error('搜索配置API请求错误:', error);
    console.log('请求URL:', error.config?.url);
    console.log('请求配置:', JSON.stringify(error.config, null, 2));
    console.log('错误状态:', error.response?.status);
    console.log('错误响应:', error.response?.data);
    return Promise.reject(error);
  }
);

// 数据库管理API
export const databaseAPI = {
  // 获取所有数据库
  getDatabases: () => api.get('/databases'),
  
  // 获取指定数据库的所有表
  getTables: (databaseName) => api.get(`/databases/${databaseName}/tables`),
  
  // 获取表结构
  getTableStructure: (databaseName, tableName) => api.get(`/tables/${databaseName}.${tableName}/structure`),
  
  // 获取表数据（支持搜索）
  getTableData: (databaseName, tableName, page = 1, pageSize = 100, searchParams = {}) => {
    // 直接使用传入的page和pageSize参数
    const params = { ...searchParams, page, pageSize };
    
    return api.get(`/tables/${databaseName}.${tableName}/data`, { params });
  },
  
  // 创建记录
  createRecord: (databaseName, tableName, record) => 
    api.post(`/tables/${databaseName}.${tableName}/data`, record),
  
  // 更新记录
  updateRecord: (databaseName, tableName, id, record) => 
    api.put(`/tables/${databaseName}.${tableName}/data/${id}`, record),
  
  // 删除记录
  deleteRecord: (databaseName, tableName, id) => 
    api.delete(`/tables/${databaseName}.${tableName}/data/${id}`)
};

// 搜索配置API
export const searchConfigAPI = {
  // 获取所有搜索配置
  getAllSearchConfigs: () => searchConfigAPIInstance.get('/'),
  
  // 根据ID获取搜索配置
  getSearchConfigById: (id) => searchConfigAPIInstance.get(`/${id}`),
  
  // 根据数据库名和表名获取搜索配置
  getSearchConfigsByDatabaseAndTable: (databaseName, tableName) => 
    searchConfigAPIInstance.get(`/database/${databaseName}/table/${tableName}`),
  
  // 根据数据库名获取搜索配置
  getSearchConfigsByDatabase: (databaseName) => 
    searchConfigAPIInstance.get(`/database/${databaseName}`),
  
  // 创建搜索配置
  createSearchConfig: (config) => searchConfigAPIInstance.post('/', config),
  
  // 更新搜索配置
  updateSearchConfig: (id, config) => searchConfigAPIInstance.put(`/${id}`, config),
  
  // 删除搜索配置
  deleteSearchConfig: (id) => searchConfigAPIInstance.delete(`/${id}`)
};

export default api;