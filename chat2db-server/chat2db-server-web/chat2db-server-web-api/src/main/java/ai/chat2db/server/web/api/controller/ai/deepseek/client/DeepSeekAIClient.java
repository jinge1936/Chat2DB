package ai.chat2db.server.web.api.controller.ai.deepseek.client;

import ai.chat2db.server.domain.api.model.Config;
import ai.chat2db.server.domain.api.service.ConfigService;
import ai.chat2db.server.web.api.util.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class DeepSeekAIClient {

    public static final String DEEPSEEK_API_KEY = "deepseek.apiKey";
    public static final String DEEPSEEK_API_HOST = "deepseek.apiHost";

    private static DeepSeekAiStreamClient DEEPSEEK_AI_STREAM_CLIENT;
    private static String apiKey;

    public static DeepSeekAiStreamClient getInstance() {
        if (DEEPSEEK_AI_STREAM_CLIENT != null) {
            return DEEPSEEK_AI_STREAM_CLIENT;
        } else {
            return singleton();
        }
    }

    private static DeepSeekAiStreamClient singleton() {
        if (DEEPSEEK_AI_STREAM_CLIENT == null) {
            synchronized (DeepSeekAIClient.class) {
                if (DEEPSEEK_AI_STREAM_CLIENT == null) {
                    refresh();
                }
            }
        }
        return DEEPSEEK_AI_STREAM_CLIENT;
    }

    public static void refresh() {
        String apikey = "";
        String apiHost = "https://api.deepseek.com/v1/chat/completions";
        
        ConfigService configService = ApplicationContextUtil.getBean(ConfigService.class);
        Config apiHostConfig = configService.find(DEEPSEEK_API_HOST).getData();
        if (apiHostConfig != null) {
            apiHost = apiHostConfig.getContent();
        }
        
        Config config = configService.find(DEEPSEEK_API_KEY).getData();
        if (config != null) {
            apikey = config.getContent();
        }

        log.info("refresh deepseek apiKey:{}", maskApiKey(apikey));
        DEEPSEEK_AI_STREAM_CLIENT = DeepSeekAiStreamClient.builder()
            .apiHost(apiHost)
            .apiKey(apikey)
            .build();
        apiKey = apikey;
    }

    private static String maskApiKey(String input) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        StringBuilder maskedString = new StringBuilder(input);
        for (int i = input.length() / 4; i < input.length() / 2; i++) {
            maskedString.setCharAt(i, '*');
        }
        return maskedString.toString();
    }
} 