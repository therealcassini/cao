import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: '/api',
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

// 主题相关API
export const themeAPI = {
  getAll: () => api.get('/themes'),
  getById: (id) => api.get(`/themes/${id}`),
  create: (theme) => api.post('/themes', theme),
  update: (id, theme) => api.put(`/themes/${id}`, theme),
  delete: (id) => api.delete(`/themes/${id}`)
};

// 节点相关API
export const nodeAPI = {
  getAll: () => api.get('/nodes'),
  getById: (id) => api.get(`/nodes/${id}`),
  getByThemeId: (themeId) => api.get(`/nodes/theme/${themeId}`),
  create: (node) => api.post('/nodes', node),
  update: (id, node) => api.put(`/nodes/${id}`, node),
  delete: (id) => api.delete(`/nodes/${id}`)
};

// 边相关API
export const edgeAPI = {
  getAll: () => api.get('/edges'),
  getById: (id) => api.get(`/edges/${id}`),
  getByNodeId: (nodeId) => api.get(`/edges/node/${nodeId}`),
  getByNodeName: (nodeName) => api.get(`/edges/node-name/${nodeName}`),
  getByThemeId: (themeId) => api.get(`/edges/theme/${themeId}`),
  create: (edge) => api.post('/edges', edge),
  update: (id, edge) => api.put(`/edges/${id}`, edge),
  delete: (id) => api.delete(`/edges/${id}`)
};

// 知识图谱相关API
export const graphAPI = {
  getFullGraph: () => api.get('/graph/full'),
  getGraphByTheme: (themeId) => api.get(`/graph/theme/${themeId}`)
};

export default api;