package com.huadetec.gamecommunity.controller;

import com.huadetec.gamecommunity.service.AiService;
import com.huadetec.gamecommunity.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    @Autowired
    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public Result ask(@RequestBody AiRequest request) {
        try {
            long startTime = System.currentTimeMillis();
            String answer = aiService.askQuestion(request.getQuestion(), request.getContext());
            long responseTime = System.currentTimeMillis() - startTime;
            // 创建响应对象，包含回答和响应时间
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("answer", answer);
            responseData.put("responseTime", (int) responseTime);
            return Result.success(responseData);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500, "AI回答生成失败，请稍后再试");
        }
    }

    // 请求对象
    public static class AiRequest {
        private String question;
        private List<String> context;

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getContext() {
            return context;
        }

        public void setContext(List<String> context) {
            this.context = context;
        }
    }
}
