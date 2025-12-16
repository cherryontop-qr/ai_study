import axios, { type InternalAxiosRequestConfig, type AxiosResponse } from 'axios';
import store from '@/store';
import type { ApiResponse } from '@/types/api';

const instance = axios.create({
  baseURL: '/api',
  timeout: 120000 // 增加到120秒，因为AI响应可能需要较长时间
});

instance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // 只对 /api 路径的请求添加 token，避免拦截静态资源
  if (config.url && config.url.startsWith('/api')) {
    const token = store.state.user.token;
    const userId = store.state.user.userId;
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
      // 添加用户ID到请求头，方便后端获取
      if (userId) {
        config.headers['X-User-Id'] = String(userId);
      }
    }
  }
  return config;
});

instance.interceptors.response.use(
  (response: AxiosResponse) => response,
  (error: any) => {
    // 只处理 /api 路径的 401 错误，避免拦截静态资源
    if (error.config?.url?.startsWith('/api') && error.response?.status === 401) {
      // 清除本地存储
      localStorage.clear();
      // 跳转到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export function get<T>(url: string, params?: unknown): Promise<ApiResponse<T>> {
  return instance.get<ApiResponse<T>>(url, { params }).then((res: AxiosResponse<ApiResponse<T>>) => res.data);
}

export function post<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return instance.post<ApiResponse<T>>(url, data).then((res: AxiosResponse<ApiResponse<T>>) => res.data);
}

export function put<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return instance.put<ApiResponse<T>>(url, data).then((res: AxiosResponse<ApiResponse<T>>) => res.data);
}

export function del<T>(url: string): Promise<ApiResponse<T>> {
  return instance.delete<ApiResponse<T>>(url).then((res: AxiosResponse<ApiResponse<T>>) => res.data);
}

