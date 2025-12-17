package com.ai.study.service.impl;

import com.ai.study.common.PageResult;
import com.ai.study.domain.StudyTask;
import com.ai.study.dto.TaskCreateRequest;
import com.ai.study.dto.TaskQueryRequest;
import com.ai.study.mapper.StudyTaskMapper;
import com.ai.study.service.StudyTaskService;
import com.ai.study.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyTaskServiceImpl implements StudyTaskService {

    private final StudyTaskMapper studyTaskMapper;
    private final StudyRecordService studyRecordService;

    @Override
    public PageResult<StudyTask> pageTasks(Long userId, TaskQueryRequest queryRequest) {
        List<StudyTask> list = studyTaskMapper.findByUserId(userId, queryRequest);
        long total = studyTaskMapper.countByUserId(userId, queryRequest);
        return new PageResult<>(list, total, queryRequest.getPageNum(), queryRequest.getPageSize());
    }

    @Override
    public PageResult<StudyTask> pageAllTasks(TaskQueryRequest queryRequest) {
        List<StudyTask> list = studyTaskMapper.findAll(queryRequest);
        long total = studyTaskMapper.countAll(queryRequest);
        return new PageResult<>(list, total, queryRequest.getPageNum(), queryRequest.getPageSize());
    }

    @Override
    @Transactional
    public StudyTask createTask(Long userId, TaskCreateRequest request) {
        StudyTask task = new StudyTask();
        BeanUtils.copyProperties(request, task);
        task.setUserId(userId);
        if (task.getStatus() == null || task.getStatus().isEmpty()) {
            task.setStatus("TODO");
        }
        task.setCreateTime(LocalDateTime.now());
        studyTaskMapper.insert(task);
        return task;
    }

    @Override
    @Transactional
    public StudyTask updateTask(Long id, TaskCreateRequest request) {
        StudyTask task = studyTaskMapper.findById(id);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        BeanUtils.copyProperties(request, task, "id", "userId", "createTime");
        studyTaskMapper.update(task);
        return studyTaskMapper.findById(id);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        // 先删除该任务关联的学习记录，避免外键约束导致删除失败
        studyRecordService.deleteByTaskId(id);
        studyTaskMapper.deleteById(id);
    }

    @Override
    public StudyTask getTaskById(Long id) {
        return studyTaskMapper.findById(id);
    }
}

