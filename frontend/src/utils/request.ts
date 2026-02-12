import axios from 'axios';
import { message } from 'antd';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        message.error('Session expired, please login again.');
        localStorage.removeItem('token');
        window.location.href = '/login';
      } else {
        message.error(data.message || 'Request failed');
      }
    } else {
      message.error('Network error');
    }
    return Promise.reject(error);
  }
);

export default request;
