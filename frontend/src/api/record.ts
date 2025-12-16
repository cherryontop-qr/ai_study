// src/api/record.ts
import { get, post, del } from './index';
import type { ApiResponse, StudyRecord, StudyRecordCreateRequest } from '@/types/api';

// 新增打卡记录
export const createRecord = (data: StudyRecordCreateRequest): Promise<ApiResponse<StudyRecord>> => {
  return post<StudyRecord>('/records', data);
};

// 删除打卡记录
export const deleteRecord = (id: number): Promise<ApiResponse<void>> => {
  return del<void>(`/records/${id}`);
};

// 根据任务查询记录
export const getRecordsByTask = (taskId: number): Promise<ApiResponse<StudyRecord[]>> => {
  return get<StudyRecord[]>(`/records/task/${taskId}`);
};

// 查询最近 7 天记录
export const getRecentRecords = (): Promise<ApiResponse<StudyRecord[]>> => {
  return get<StudyRecord[]>('/records/recent');
};

