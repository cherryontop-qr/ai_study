package com.ai.study.service;

import com.ai.study.common.PageResult;
import com.ai.study.domain.StudyTask;
import com.ai.study.dto.TaskCreateRequest;
import com.ai.study.dto.TaskQueryRequest;

public interface StudyTaskService {

    PageResult<StudyTask> pageTasks(Long userId, TaskQueryRequest queryRequest);

    PageResult<StudyTask> pageAllTasks(TaskQueryRequest queryRequest);

    StudyTask createTask(Long userId, TaskCreateRequest request);

    StudyTask updateTask(Long id, TaskCreateRequest request);

    void deleteTask(Long id);

    StudyTask getTaskById(Long id);
}

