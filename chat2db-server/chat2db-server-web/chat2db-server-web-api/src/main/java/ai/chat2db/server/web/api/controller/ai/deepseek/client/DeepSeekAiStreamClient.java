package ai.chat2db.server.web.api.controller.ai.deepseek.client;

import ai.chat2db.server.tools.common.exception.ParamBusinessException;
import ai.chat2db.server.web.api.controller.ai.deepseek.interceptor.DeepSeekHeaderAuthorizationInterceptor;
import ai.chat2db.server.web.api.controller.ai.deepseek.model.DeepSeekChatMessage;
import cn.hutool.http.ContentType;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DeepSeekAiStreamClient {

    @Getter
    @NotNull
    private String apiKey;

    @Getter
    @NotNull
    private String apiHost;

    @Getter
    private OkHttpClient okHttpClient;

    private DeepSeekAiStreamClient(Builder builder) {
        this.apiKey = builder.apiKey;
        this.apiHost = builder.apiHost;
        if (Objects.isNull(builder.okHttpClient)) {
            builder.okHttpClient = this.okHttpClient();
        }
        okHttpClient = builder.okHttpClient;
    }

    private OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
            .addInterceptor(new DeepSeekHeaderAuthorizationInterceptor(this.apiKey))
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(50, TimeUnit.SECONDS)
            .readTimeout(50, TimeUnit.SECONDS)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private String apiHost;
        private OkHttpClient okHttpClient;

        public Builder() {
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiHost(String apiHost) {
            this.apiHost = apiHost;
            return this;
        }

        public Builder okHttpClient(OkHttpClient val) {
            this.okHttpClient = val;
            return this;
        }

        public DeepSeekAiStreamClient build() {
            return new DeepSeekAiStreamClient(this);
        }
    }

    public void streamCompletions(DeepSeekChatMessage chatMessage, EventSourceListener eventSourceListener) {
        if (Objects.isNull(eventSourceListener)) {
            log.error("param error：EventSourceListener cannot be empty");
            throw new ParamBusinessException();
        }
        
        log.info("DeepSeek AI, prompt:{}", chatMessage.getMessages());
        try {
            EventSource.Factory factory = EventSources.createFactory(this.okHttpClient);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            String requestBody = mapper.writeValueAsString(chatMessage);

            Request request = new Request.Builder()
                .url(this.apiHost)
                .post(RequestBody.create(MediaType.parse(ContentType.JSON.getValue()), requestBody))
                .build();

            EventSource eventSource = factory.newEventSource(request, eventSourceListener);
            log.info("finish invoking deepseek ai");
        } catch (Exception e) {
            log.error("deepseek ai error", e);
            eventSourceListener.onFailure(null, e, null);
            throw new ParamBusinessException();
        }
    }
} 