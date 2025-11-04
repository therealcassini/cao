import axios from 'axios';
import { message } from 'ant-design-vue';

// 创建axios实例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
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
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    // 直接返回响应数据
    return response.data;
  },
  error => {
    console.error('响应错误:', error);
    
    // 错误处理
    if (error.response) {
      // 服务器返回错误状态码
      const status = error.response.status;
      switch (status) {
        case 401:
          message.error('未授权，请重新登录');
          break;
        case 403:
          message.error('拒绝访问');
          break;
        case 404:
          message.error('请求的资源不存在');
          break;
        case 500:
          message.error('服务器内部错误');
          break;
        default:
          message.error(`请求失败: ${error.response.data?.message || error.message}`);
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message.error('网络错误，请检查网络连接');
    } else {
      // 请求配置出错
      message.error('请求配置错误');
    }
    
    return Promise.reject(error);
  }
);

// 时间线API服务
export const timelineAPI = {
  // 获取历史人物数据
  async getPersons() {
    try {
      return await api.get('/timeline/persons');
    } catch (error) {
      console.error('获取历史人物数据失败:', error);
      // 返回空数组作为后备
      return [];
    }
  },
  
  // 获取历史事件数据
  async getEvents() {
    try {
      return await api.get('/timeline/events');
    } catch (error) {
      console.error('获取历史事件数据失败:', error);
      // 返回空数组作为后备
      return [];
    }
  },
  
  // 根据年份获取历史事件
  async getEventsByYear(year) {
    try {
      return await api.get(`/timeline/events/year/${year}`);
    } catch (error) {
      console.error(`获取${year}年历史事件失败:`, error);
      return [];
    }
  },
  
  // 根据人物ID获取相关事件
  async getEventsByPerson(personId) {
    try {
      return await api.get(`/timeline/events/person/${personId}`);
    } catch (error) {
      console.error(`获取人物${personId}相关事件失败:`, error);
      return [];
    }
  },
  
  // 根据事件类型筛选事件
  async getEventsByType(type) {
    try {
      return await api.get(`/timeline/events/type/${type}`);
    } catch (error) {
      console.error(`获取${type}类型事件失败:`, error);
      return [];
    }
  }
};

export default api;