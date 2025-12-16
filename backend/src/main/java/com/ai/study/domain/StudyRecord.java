package com.ai.study.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyRecord {
    private Long id;
    private Long taskId;
    private LocalDate studyDate;
    private Integer durationMinutes;
    private String comment;
    private LocalDateTime createTime;
}

