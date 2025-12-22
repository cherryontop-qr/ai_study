package com.ai.study.client;

import com.ai.study.config.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeepSeekClient {

    private final RestTemplate restTemplate;
    private final DeepSeekProperties properties;

    public String chat(String prompt) {
        // 验证配置
        if (properties.getApiKey() == null || properties.getApiKey().isEmpty()) {
            log.error("DeepSeek API Key 未配置");
            return "AI 服务配置错误：API Key 未设置";
        }
        if (properties.getBaseUrl() == null || properties.getBaseUrl().isEmpty()) {
            log.error("DeepSeek Base URL 未配置");
            return "AI 服务配置错误：Base URL 未设置";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String apiKey = properties.getApiKey();
        if (apiKey != null && !apiKey.startsWith("Bearer ")) {
            headers.set("Authorization", "Bearer " + apiKey);
        } else {
            headers.set("Authorization", apiKey);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel() != null ? properties.getModel() : "deepseek-chat");
        body.put("messages", List.of(
                Map.of("role", "system", "content", "你是一个专业的学习规划 AI 教练"),
                Map.of("role", "user", "content", prompt)
        ));

        // 调用DeepSeek API！
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            String apiUrl = properties.getBaseUrl();
            if (!apiUrl.contains("/chat/completions")) {
                apiUrl = apiUrl.endsWith("/") ? apiUrl + "chat/completions" : apiUrl + "/chat/completions";
            }

            log.info("调用 AI API: {}", apiUrl);

            Map<String, Object> response = restTemplate.postForObject(
                    apiUrl, entity, Map.class);

            if (response == null) {
                log.warn("DeepSeek API 返回 null");
                return "AI 服务暂不可用，请稍后再试。";
            }

            // 检查是否有错误
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMsg = error != null ? (String) error.get("message") : "未知错误";
                log.error("DeepSeek API 返回错误: {}", errorMsg);
                return "AI 服务错误：" + errorMsg;
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                log.warn("DeepSeek API 返回的 choices 为空");
                return "AI 服务返回结果为空。";
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = message != null ? (String) message.get("content") : null;
            if (content == null) {
                log.warn("DeepSeek API 返回的 content 为空");
                return "AI 服务返回内容为空。";
            }
            return content;
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("调用 DeepSeek API HTTP 错误: {} - {}", e.getStatusCode(), e.getStatusText());
            return "AI 服务调用失败：" + e.getStatusCode() + " - " + e.getStatusText();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("调用 DeepSeek API 网络错误", e);
            return "AI 服务网络连接失败，请检查网络连接或稍后重试。";
        } catch (Exception e) {
            log.error("调用 DeepSeek 失败", e);
            return "AI 服务调用失败：" + e.getMessage();
        }
    }
}




