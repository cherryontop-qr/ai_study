package com.ai.study.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiTaskPlanResponse {
    private String summary;
    private List<AiPlannedTask> tasks;

    @Data
    public static class AiPlannedTask {
        private String title;
        private String description;
        private Integer suggestedHours;
    }
}


