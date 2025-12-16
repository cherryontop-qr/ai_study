package com.ai.study.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RecordCreateRequest {
    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "学习日期不能为空")
    private LocalDate studyDate;

    @NotNull(message = "学习时长不能为空")
    @Positive(message = "学习时长必须大于0")
    private Integer durationMinutes;

    private String comment;
}

