package com.ai.study.controller;

import com.ai.study.common.ApiResponse;
import com.ai.study.domain.StudyRecord;
import com.ai.study.dto.RecordCreateRequest;
import com.ai.study.service.StudyRecordService;
import com.ai.study.util.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@Tag(name = "打卡记录管理")
@RequiredArgsConstructor
public class RecordController {

    private final StudyRecordService studyRecordService;

    private Long getCurrentUserId() {
        return UserContext.getUserId();
    }

    @Operation(summary = "新增打卡记录")
    @PostMapping
    public ApiResponse<StudyRecord> createRecord(@RequestBody @Valid RecordCreateRequest request) {
        StudyRecord record = new StudyRecord();
        BeanUtils.copyProperties(request, record);
        return ApiResponse.success(studyRecordService.createRecord(record));
    }

    @Operation(summary = "删除打卡记录")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        studyRecordService.deleteRecord(id);
        return ApiResponse.success(null);
    }

    @Operation(summary = "根据任务查询记录")
    @GetMapping("/task/{taskId}")
    public ApiResponse<List<StudyRecord>> listByTask(@PathVariable Long taskId) {
        return ApiResponse.success(studyRecordService.listByTaskId(taskId));
    }

    @Operation(summary = "查询最近 7 天记录")
    @GetMapping("/recent")
    public ApiResponse<List<StudyRecord>> listRecent() {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return ApiResponse.success(studyRecordService.listByUserAndDateRange(
                getCurrentUserId(), start, end));
    }
}

