// src/api/auth.ts
import axios from 'axios';
import type { AxiosResponse } from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 请求拦截器
api.interceptors.request.use(
    config => {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    error => {
      return Promise.reject(error);
    }
);

// 响应拦截器
api.interceptors.response.use(
    response => response.data,
    error => {
      if (error.response?.status === 401) {
        localStorage.clear();
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
);

// 登录接口
export interface LoginParams {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  nickname: string;
  role: string;
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export const loginApi = (data: LoginParams): Promise<ApiResponse<LoginResponse>> => {
  return api.post('/auth/login', data);
};

// 注册接口
export interface RegisterParams {
  username: string;
  nickname: string;
  password: string;
}

export const registerApi = (data: RegisterParams): Promise<ApiResponse<void>> => {
  return api.post('/auth/register', data);
};



