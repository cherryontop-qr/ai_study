package com.ai.study.dto;

import lombok.Data;

@Data
public class TaskQueryRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String keyword;
    private String status;
    private String category;
}

