package com.example.gateway.application.chat;

import com.example.gateway.api.dto.ChatDtos;
import com.example.gateway.domain.chat.Chat;
import com.example.gateway.domain.chat.ChatMessage;
import com.example.gateway.infra.fastapi.FastApiClient;
import com.example.gateway.infra.mongo.ChatRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;


import static com.example.gateway.application.ids.Ids.newId;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final FastApiClient fastApiClient;

    public ChatService(ChatRepository chatRepository, FastApiClient fastApiClient) {
        this.chatRepository = chatRepository;
        this.fastApiClient = fastApiClient;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static String json(Map<String, Object> map) {
        try { return MAPPER.writeValueAsString(map); }
        catch (Exception e) { return "{\"type\":\"error\",\"content\":{\"message\":\"json_serialize_failed\"}}"; }
    }

    public Mono<ChatDtos.ChatTitleResponse> getTitle(String chatId) {
        return chatRepository.findByChatId(chatId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("chatId not found")))
                .map(c -> {
                    var resp = new ChatDtos.ChatTitleResponse();
                    resp.title = c.getTitle();
                    return resp;
                });
    }

    public Mono<ChatDtos.ChatHistoryResponse> getHistory(String chatId) {
        return chatRepository.findByChatId(chatId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("chatId not found")))
                .map(c -> {
                    var resp = new ChatDtos.ChatHistoryResponse();
                    resp.chatId = c.getChatId();
                    resp.messages = c.getMessages().stream().map(m -> {
                        var dto = new ChatDtos.Message();
                        dto.speaker = m.getSpeaker();
                        dto.content = m.getContent();
                        return dto;
                    }).toList();
                    return resp;
                });
    }

    public Mono<Void> deleteChat(String chatId) {
        return chatRepository.deleteByChatId(chatId);
    }

    public Flux<ServerSentEvent<String>> newChat(String userMessage) {
        return fastApiClient.streamNewChat(userMessage, "test-user-id", "Test Company")
                .doOnNext(sse -> {
                    String ev = sse.event();
                    String data = sse.data();
                    if ("chunk".equals(ev)) {
                        System.out.println("[GW->FE] OUT event=chunk dataPreview=" + clip(data, 200));
                    } else {
                        System.out.println("[GW->FE] OUT event=" + ev + " len=" + (data != null ? data.length() : 0));
                    }
                })
                .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(json(Map.of("type","error","content", Map.of("message", ex.getMessage()))))
                        .build()));
     }

    public Flux<ServerSentEvent<String>> continueChat(String chatId, String message) {
        return fastApiClient.streamChat(chatId, message, "test-user-id", "Test Company")
                .doOnNext(sse -> {
                    String ev = sse.event();
                    String data = sse.data();
                    if ("chunk".equals(ev)) {
                        System.out.println("[GW->FE] OUT chatId=" + chatId + " event=chunk dataPreview=" + clip(data, 200));
                    } else {
                        System.out.println("[GW->FE] OUT chatId=" + chatId + " event=" + ev + " len=" + (data != null ? data.length() : 0));
                    }
                })
                .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder()
                        .event("error")
                        .data(json(Map.of("type","error","content", Map.of("message", ex.getMessage()))))
                        .build()));
                    }

    private static String clip(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max) + "...";
    }
}