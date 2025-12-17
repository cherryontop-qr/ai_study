package com.ai.study.service.impl;

import com.ai.study.client.DeepSeekClient;
import com.ai.study.domain.StudyRecord;
import com.ai.study.dto.AiTaskPlanRequest;
import com.ai.study.dto.AiTaskPlanResponse;
import com.ai.study.service.AiService;
import com.ai.study.service.StudyRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final DeepSeekClient deepSeekClient;
    private final StudyRecordService studyRecordService;
    private final ObjectMapper objectMapper;

    @Override
    public String generateWeeklySuggestion(Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        List<StudyRecord> records = studyRecordService.listByUserAndDateRange(userId, start, end);
        String dataset = records.isEmpty()
                ? "最近 7 天没有任何学习记录。"
                : records.stream()
                .map(r -> String.format("日期:%s, 学习时长:%d 分钟, 学习内容或备注:%s",
                        r.getStudyDate(), r.getDurationMinutes(),
                        r.getComment() == null ? "" : r.getComment()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                你是一名学习规划与时间管理教练。\n\
                - 根据下面最近 7 天的学习记录，分析学员的总体学习时间投入、节奏和稳定性；\n\
                - 结合「今日任务完成情况」和「全部学习任务总体完成度」这两个维度，给出具体可执行的优化建议，\
                  包括今天之后 3~5 天应该如何安排学习任务；\n\
                - 输出结构建议包含：本周学习情况总结、当前任务完成度分析、下一步行动建议三个小节。\n\
                请用中文回答，语气鼓励、具体，不要出现“打卡”两个字。\n\
                最近 7 天的学习记录如下：
                %s
                """.formatted(dataset);

        return deepSeekClient.chat(prompt);
    }

    @Override
    public AiTaskPlanResponse generateTaskPlan(Long userId, AiTaskPlanRequest request) {
        String prompt = """
                目标描述：%s
                期望任务数量：%d
                个性偏好/擅长方向：%s

                请以 JSON 形式严格返回以下结构：
                {
                  "summary": "对整体学习计划的简短总结",
                  "tasks": [
                    {
                      "title": "任务标题",
                      "description": "任务说明，含具体行动",
                      "suggestedHours": 3
                    }
                  ]
                }
                """.formatted(request.getGoalDescription(),
                request.getTaskCount(),
                request.getPreference() == null ? "未提供" : request.getPreference());

        String responseText = deepSeekClient.chat(prompt);
        try {
            // 清理响应文本，移除可能的 markdown 代码块标记
            String cleanedText = responseText.trim();

            // 移除 ```json 和 ``` 标记
            if (cleanedText.startsWith("```json")) {
                cleanedText = cleanedText.substring(7).trim();
            } else if (cleanedText.startsWith("```")) {
                cleanedText = cleanedText.substring(3).trim();
            }
            if (cleanedText.endsWith("```")) {
                cleanedText = cleanedText.substring(0, cleanedText.length() - 3).trim();
            }

            // 尝试查找 JSON 对象
            int jsonStart = cleanedText.indexOf("{");
            int jsonEnd = cleanedText.lastIndexOf("}");
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanedText = cleanedText.substring(jsonStart, jsonEnd + 1);
            }

            log.debug("清理后的 JSON 文本: {}", cleanedText);

            AiTaskPlanResponse result = objectMapper.readValue(cleanedText, AiTaskPlanResponse.class);

            // 验证结果
            if (result.getTasks() == null || result.getTasks().isEmpty()) {
                log.warn("AI 返回的任务列表为空");
                result.setTasks(Collections.emptyList());
            } else {
                log.info("成功解析 {} 个任务", result.getTasks().size());
            }

            return result;
        } catch (Exception e) {
            log.error("解析 AI 任务计划失败，原始响应: {}", responseText, e);
            AiTaskPlanResponse fallback = new AiTaskPlanResponse();
            fallback.setSummary("AI 返回内容无法解析为任务计划。\n原始响应：\n" + responseText);
            fallback.setTasks(Collections.emptyList());
            return fallback;
        }
    }
}


