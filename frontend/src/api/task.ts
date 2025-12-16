// src/api/task.ts
import { get, post, put, del } from './index';
import type { ApiResponse, PageResult, Task, TaskQuery } from '@/types/api';

// 分页查询任务
export const getTaskPage = (params: TaskQuery): Promise<ApiResponse<PageResult<Task>>> => {
  return get<PageResult<Task>>('/tasks', params);
};

// 获取任务详情
export const getTask = (id: number): Promise<ApiResponse<Task>> => {
  return get<Task>(`/tasks/${id}`);
};

// 创建任务
export const createTask = (data: {
  title: string;
  description?: string;
  targetHours: number;
  category?: string;
  deadline?: string;
}): Promise<ApiResponse<Task>> => {
  return post<Task>('/tasks', data);
};

// 更新任务
export const updateTask = (id: number, data: {
  title?: string;
  description?: string;
  targetHours?: number;
  category?: string;
  status?: 'TODO' | 'DOING' | 'DONE';
  deadline?: string;
}): Promise<ApiResponse<Task>> => {
  return put<Task>(`/tasks/${id}`, data);
};

// 删除任务
export const deleteTask = (id: number): Promise<ApiResponse<void>> => {
  return del<void>(`/tasks/${id}`);
};

