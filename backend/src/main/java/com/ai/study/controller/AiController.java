package com.ai.study.controller;

import com.ai.study.common.ApiResponse;
import com.ai.study.dto.AiTaskPlanRequest;
import com.ai.study.dto.AiTaskPlanResponse;
import com.ai.study.service.AiService;
import com.ai.study.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI 学习教练")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    @Operation(summary = "获取最近 7 天学习建议")
    @GetMapping("/suggestion/weekly")
    public ApiResponse<String> weeklySuggestion() {
        return ApiResponse.success(aiService.generateWeeklySuggestion(getCurrentUserId()));
    }

    @Operation(summary = "根据目标生成任务计划")
    @PostMapping("/tasks/plan")
    public ApiResponse<AiTaskPlanResponse> generateTaskPlan(@RequestBody @Valid AiTaskPlanRequest request) {
        return ApiResponse.success(aiService.generateTaskPlan(getCurrentUserId(), request));
    }
}




