package com.ai.study.mapper;

import com.ai.study.domain.StudyTask;
import com.ai.study.dto.TaskQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StudyTaskMapper {

    int insert(StudyTask task);

    int update(StudyTask task);

    int deleteById(Long id);

    StudyTask findById(Long id);

    List<StudyTask> findByUserId(@Param("userId") Long userId, @Param("query") TaskQueryRequest query);

    long countByUserId(@Param("userId") Long userId, @Param("query") TaskQueryRequest query);

    List<StudyTask> findAll(@Param("query") TaskQueryRequest query);

    long countAll(@Param("query") TaskQueryRequest query);
}

