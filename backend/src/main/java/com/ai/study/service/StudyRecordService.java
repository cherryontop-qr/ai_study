package com.ai.study.service;

import com.ai.study.domain.StudyRecord;
import com.ai.study.dto.TaskProgressResponse;
import java.time.LocalDate;
import java.util.List;

public interface StudyRecordService {

    StudyRecord createRecord(StudyRecord record);

    void deleteRecord(Long id);

    void deleteByTaskId(Long taskId);

    List<StudyRecord> listByTaskId(Long taskId);

    List<StudyRecord> listByUserAndDateRange(Long userId, LocalDate start, LocalDate end);

    /**
     * 查询当前用户每个任务累计学习时长（分钟）
     */
    List<TaskProgressResponse> sumDurationByUserId(Long userId);
}

