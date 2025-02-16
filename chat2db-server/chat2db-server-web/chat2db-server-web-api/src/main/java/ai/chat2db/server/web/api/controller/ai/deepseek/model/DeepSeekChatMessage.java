package ai.chat2db.server.web.api.controller.ai.deepseek.model;

import java.util.List;
import lombok.Data;

@Data
public class DeepSeekChatMessage {
    private String model = "deepseek-chat";
    private List<Message> messages;
    private boolean stream = true;
    private double temperature = 0.7;
    private int max_tokens = 2000;

    @Data
    public static class Message {
        private String role;
        private String content;
    }
} 