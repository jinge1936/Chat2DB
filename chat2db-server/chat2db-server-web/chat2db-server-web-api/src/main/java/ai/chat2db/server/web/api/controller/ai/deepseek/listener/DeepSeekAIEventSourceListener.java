package ai.chat2db.server.web.api.controller.ai.deepseek.listener;

import ai.chat2db.server.web.api.controller.ai.deepseek.model.DeepSeekCompletionResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unfbx.chatgpt.entity.chat.Message;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Objects;

@Slf4j
public class DeepSeekAIEventSourceListener extends EventSourceListener {

    private SseEmitter sseEmitter;
    private ObjectMapper mapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public DeepSeekAIEventSourceListener(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    @Override
    public void onOpen(EventSource eventSource, Response response) {
        log.info("DeepSeek AI EventSource opened");
    }

    @SneakyThrows
    @Override
    public void onEvent(EventSource eventSource, String id, String type, String data) {
        log.info("DeepSeek AI data：{}", data);
        if (data.equals("[DONE]")) {
            log.info("DeepSeek AI completed");
            sseEmitter.send(SseEmitter.event()
                .id("[DONE]")
                .data("[DONE]")
                .reconnectTime(3000));
            sseEmitter.complete();
            return;
        }

        DeepSeekCompletionResponse response = mapper.readValue(data, DeepSeekCompletionResponse.class);
        String text = response.getChoices().get(0).getDelta().getContent();
        if (text != null) {
            Message message = new Message();
            message.setContent(text);
            sseEmitter.send(SseEmitter.event()
                .id(null)
                .data(message)
                .reconnectTime(3000));
        }
    }

    @Override
    public void onClosed(EventSource eventSource) {
        log.info("DeepSeek AI EventSource closed");
        sseEmitter.complete();
    }

    @Override
    public void onFailure(EventSource eventSource, Throwable t, Response response) {
        try {
            if (Objects.isNull(response)) {
                String message = t.getMessage();
                Message sseMessage = new Message();
                sseMessage.setContent(message);
                sseEmitter.send(SseEmitter.event()
                    .id("[ERROR]")
                    .data(sseMessage));
                sseEmitter.send(SseEmitter.event()
                    .id("[DONE]")
                    .data("[DONE]"));
                sseEmitter.complete();
                return;
            }
            ResponseBody body = response.body();
            String bodyString = null;
            if (Objects.nonNull(body)) {
                bodyString = body.string();
                log.error("DeepSeek AI SSE error：{}", bodyString, t);
            } else {
                log.error("DeepSeek AI SSE body error：{}", response, t);
            }
            eventSource.cancel();
            Message message = new Message();
            message.setContent("DeepSeek AI error：" + bodyString);
            sseEmitter.send(SseEmitter.event()
                .id("[ERROR]")
                .data(message));
            sseEmitter.send(SseEmitter.event()
                .id("[DONE]")
                .data("[DONE]"));
            sseEmitter.complete();
        } catch (Exception e) {
            log.error("Exception in sending data:", e);
        }
    }
} 