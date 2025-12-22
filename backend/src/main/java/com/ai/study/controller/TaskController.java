package com.ai.study.controller;

import com.ai.study.common.ApiResponse;
import com.ai.study.common.PageResult;
import com.ai.study.domain.StudyTask;
import com.ai.study.dto.TaskCreateRequest;
import com.ai.study.dto.TaskQueryRequest;
import com.ai.study.service.StudyTaskService;
import com.ai.study.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "任务管理")
@RequiredArgsConstructor
public class TaskController {

    private final StudyTaskService studyTaskService;

    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    @Operation(summary = "分页查询任务")
    // 分页查询任务接口
    @GetMapping
    public ApiResponse<PageResult<StudyTask>> pageTasks(TaskQueryRequest queryRequest) {
        Long userId = getCurrentUserId();
        // 调用服务层查询
        PageResult<StudyTask> page = studyTaskService.pageTasks(userId, queryRequest);
        return ApiResponse.success(page);
    }

    @Operation(summary = "新建任务")
    @PostMapping
    public ApiResponse<StudyTask> createTask(@RequestBody @Valid TaskCreateRequest request) {
        Long userId = getCurrentUserId();
        StudyTask task = studyTaskService.createTask(userId, request);
        return ApiResponse.success(task);
    }

    @Operation(summary = "更新任务")
    @PutMapping("/{id}")
    public ApiResponse<StudyTask> updateTask(@PathVariable Long id,
                                             @RequestBody TaskCreateRequest request) {
        StudyTask task = studyTaskService.updateTask(id, request);
        return ApiResponse.success(task);
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        studyTaskService.deleteTask(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "获取任务详情")
    @GetMapping("/{id}")
    public ApiResponse<StudyTask> getTask(@PathVariable Long id) {
        return ApiResponse.success(studyTaskService.getTaskById(id));
    }
}

