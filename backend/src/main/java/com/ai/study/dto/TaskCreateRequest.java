package com.ai.study.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "任务标题不能为空")
    private String title;

    private String description;

    @NotNull(message = "目标学习时长不能为空")
    @Positive(message = "目标学习时长必须大于0")
    private Integer targetHours;

    private String category;

    private String status; // TODO, DOING, DONE

    private LocalDate deadline;
}

