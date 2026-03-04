import axios from 'axios';
import { showToast } from 'vant';

const request = axios.create({
  baseURL: '', // The dev server proxies /api
  timeout: 10000
});

// Request Interceptor
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Response Interceptor
request.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    console.error('Request Error:', error);
    if (error.response) {
      const status = error.response.status;
      if (status === 401) {
        showToast('登录状态过期，请重新登录');
        // Clear token and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        localStorage.removeItem('nickname');
        localStorage.removeItem('isGuest');
        // Wait briefly then redirect (can also use router)
        setTimeout(() => {
          window.location.href = '/login';
        }, 1500);
      } else {
        showToast(error.response.data || error.response.data.message || '请求失败');
      }
    } else {
      showToast('网络错误');
    }
    return Promise.reject(error);
  }
);

export default request;
