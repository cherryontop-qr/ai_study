// src/api/ai.ts
import { get, post } from './index';
import type { ApiResponse, AiTaskPlanRequest, AiTaskPlanResponse } from '@/types/api';

// 获取最近 7 天学习建议
export const getWeeklySuggestion = (): Promise<ApiResponse<string>> => {
  return get<string>('/ai/suggestion/weekly');
};

// 根据目标生成任务计划
export const generateTaskPlan = (data: AiTaskPlanRequest): Promise<ApiResponse<AiTaskPlanResponse>> => {
  return post<AiTaskPlanResponse>('/ai/tasks/plan', data);
};




