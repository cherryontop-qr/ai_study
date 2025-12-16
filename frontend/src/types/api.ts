// src/types/api.ts
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: number;
  nickname: string;
  role: string;
}

export interface Task {
  id: number;
  title: string;
  description: string;
  targetHours: number;
  category?: string;
  status: 'TODO' | 'DOING' | 'DONE';
  deadline: string;
  createTime: string;
}

export interface TaskQuery {
  pageNum: number;
  pageSize: number;
  keyword: string;
  status: string;
}

export interface PageResult<T> {
  list: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

export interface StudyRecord {
  id: number;
  taskId: number;
  studyDate: string;
  durationMinutes: number;
  comment: string;
}

export interface StudyRecordCreateRequest {
  taskId: number;
  studyDate: string;
  durationMinutes: number;
  comment: string;
}

export interface AiTaskPlanRequest {
  goalDescription: string;
  taskCount: number;
  preference: string;
}

export interface AiPlannedTask {
  title: string;
  description: string;
  suggestedHours: number;
}

export interface AiTaskPlanResponse {
  summary?: string;
  tasks: AiPlannedTask[];
}