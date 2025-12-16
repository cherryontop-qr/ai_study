package com.ai.study.service.impl;

import com.ai.study.domain.StudyRecord;
import com.ai.study.mapper.StudyRecordMapper;
import com.ai.study.service.StudyRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl implements StudyRecordService {

    private final StudyRecordMapper studyRecordMapper;

    @Override
    @Transactional
    public StudyRecord createRecord(StudyRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        studyRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public void deleteRecord(Long id) {
        studyRecordMapper.deleteById(id);
    }

    @Override
    public List<StudyRecord> listByTaskId(Long taskId) {
        return studyRecordMapper.findByTaskId(taskId);
    }

    @Override
    public List<StudyRecord> listByUserAndDateRange(Long userId, LocalDate start, LocalDate end) {
        return studyRecordMapper.findByUserIdAndDateRange(userId, start, end);
    }
}

