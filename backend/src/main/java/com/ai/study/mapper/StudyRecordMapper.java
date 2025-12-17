package com.ai.study.mapper;

import com.ai.study.domain.StudyRecord;
import com.ai.study.dto.TaskProgressResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudyRecordMapper {

    int insert(StudyRecord record);

    int deleteById(Long id);

    int deleteByTaskId(Long taskId);

    List<StudyRecord> findByTaskId(Long taskId);

    List<StudyRecord> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);

    List<TaskProgressResponse> sumDurationByUserId(@Param("userId") Long userId);
}

