package com.ai.study.domain;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudyTask {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private Integer targetHours;
    private String status; // TODO, DOING, DONE
    private String category;
    private LocalDate deadline;
    private LocalDateTime createTime;
}

