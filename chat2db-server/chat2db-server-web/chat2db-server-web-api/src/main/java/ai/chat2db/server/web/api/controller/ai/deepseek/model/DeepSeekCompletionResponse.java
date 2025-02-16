package ai.chat2db.server.web.api.controller.ai.deepseek.model;

import lombok.Data;
import java.util.List;

@Data
public class DeepSeekCompletionResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;

    @Data
    public static class Choice {
        private Delta delta;
        private String finishReason;
        private int index;
    }

    @Data
    public static class Delta {
        private String content;
        private String role;
    }

    @Data
    public static class Usage {
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
    }
} 