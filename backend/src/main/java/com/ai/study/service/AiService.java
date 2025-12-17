package com.ai.study.service;

import com.ai.study.dto.AiTaskPlanRequest;
import com.ai.study.dto.AiTaskPlanResponse;

public interface AiService {

    String generateWeeklySuggestion(Long userId);

    AiTaskPlanResponse generateTaskPlan(Long userId, AiTaskPlanRequest request);
}

