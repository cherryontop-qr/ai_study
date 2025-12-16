package com.ai.study.service;

import com.ai.study.domain.StudyRecord;
import java.time.LocalDate;
import java.util.List;

public interface StudyRecordService {

    StudyRecord createRecord(StudyRecord record);

    void deleteRecord(Long id);

    List<StudyRecord> listByTaskId(Long taskId);

    List<StudyRecord> listByUserAndDateRange(Long userId, LocalDate start, LocalDate end);
}

