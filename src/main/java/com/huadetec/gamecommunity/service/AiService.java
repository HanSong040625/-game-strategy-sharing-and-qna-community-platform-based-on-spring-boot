package com.huadetec.gamecommunity.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AiService {

    @Value("${baidu.qianfan.app-id}")
    private String appId;

    @Value("${baidu.qianfan.api-key}")
    private String apiKey;

    @Value("${baidu.qianfan.api-url}")
    private String apiUrl;

    private final OkHttpClient client = new OkHttpClient().newBuilder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askQuestion(String question, List<String> context) throws IOException {
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("app_id", appId);
        requestBody.put("query", buildPrompt(question, context));
        requestBody.put("stream", false);

        // 构建请求
        Request request = new Request.Builder()
                .url(apiUrl)
                .method("POST", RequestBody.create(
                        objectMapper.writeValueAsString(requestBody),
                        MediaType.parse("application/json")
                ))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        // 发送请求
        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();
            
            // 打印详细的响应信息
            System.out.println("百度千帆API响应状态码: " + response.code());
            System.out.println("百度千帆API响应内容: " + responseBody);
            
            if (!response.isSuccessful()) {
                throw new IOException("API调用失败，状态码: " + response.code() + "，响应: " + responseBody);
            }
            
            // 解析响应
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, Map.class);
            // 具体响应结构需要根据百度千帆API文档调整
            if (responseMap.containsKey("answer")) {
                return (String) responseMap.get("answer");
            } else if (responseMap.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) responseMap.get("error");
                String errorMessage = error.get("message") != null ? (String) error.get("message") : "未知错误";
                throw new IOException("API Error: " + errorMessage);
            } else if (responseMap.containsKey("data")) {
                // 检查是否有data字段
                Map<String, Object> data = (Map<String, Object>) responseMap.get("data");
                if (data.containsKey("answer")) {
                    return (String) data.get("answer");
                }
            }
            return "抱歉，无法生成回答，请稍后再试。";
        }
    }

    // 构建包含上下文的 prompt
    private String buildPrompt(String question, List<String> context) {
        StringBuilder prompt = new StringBuilder();
        if (context != null && !context.isEmpty()) {
            prompt.append("上下文：\n");
            for (String item : context) {
                prompt.append("- " + item + "\n");
            }
            prompt.append("\n");
        }
        prompt.append("问题：" + question);
        return prompt.toString();
    }
}
