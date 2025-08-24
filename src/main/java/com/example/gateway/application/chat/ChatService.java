package com.example.gateway.application.chat;

import com.example.gateway.api.dto.ChatDtos;
import com.example.gateway.domain.chat.Chat;
import com.example.gateway.domain.chat.ChatMessage;
import com.example.gateway.application.auth.JwtService;
import com.example.gateway.infra.fastapi.FastApiClient;
import com.example.gateway.infra.mongo.ChatRepository;
import com.example.gateway.infra.mongo.MessageRepository;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ChatService {
    private final JwtService jwtService;

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final FastApiClient fastApiClient;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, FastApiClient fastApiClient, JwtService jwtService) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.fastApiClient = fastApiClient;
        this.jwtService = jwtService;
    }

    private record UserCtx(String userId, String company) {}

    private Mono<UserCtx> currentUser() {
        return ReactiveSecurityContextHolder.getContext()
            .map(ctx -> {
                var auth = ctx.getAuthentication();
                if (auth == null) throw new IllegalStateException("Unauthenticated");
                String userId = auth.getName(); 
                String company = "Unknown";
    
                Object creds = auth.getCredentials();
                if (creds != null) {
                    try {
                        Claims claims = jwtService.parse(String.valueOf(creds));
                        String c = claims.get("company", String.class);
                        if (c != null && !c.isBlank()) company = c;
                    } catch (Exception ignore) {}
                }
                return new UserCtx(userId, company);
            });
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
                .flatMap(chat -> {
                    return messageRepository.findByChatIdOrderByTimestampAsc(chatId)
                        .collectList()
                        .map(messages -> {
                            var resp = new ChatDtos.ChatHistoryResponse();
                            resp.chatId = chat.getChatId();
                            resp.messages = messages.stream()
                                .map(m -> {
                                    var dto = new ChatDtos.Message();
                                    dto.speaker = m.getSpeaker();
                                    dto.content = m.getContent();
                                    dto.graph = m.getGraph();
                                    dto.plan = m.getPlan();
                                    return dto;
                                })
                                .toList();
                            return resp;
                        });
                });
    }

    public Mono<Void> deleteChat(String chatId) {
        return chatRepository.deleteByChatId(chatId);
    }

    public Mono<ChatDtos.NewChatResponse> newChat(String message) {
        return currentUser().flatMap(uc -> fastApiClient.newChat(uc.userId(), uc.company(), message))
                .map(resp -> {
                    var dto = new ChatDtos.NewChatResponse();
                    dto.chatId = (String) resp.get("chatId");
                    return dto;
                });
    }

    // public Flux<ServerSentEvent<String>> newChat(String userMessage) {
    //     return currentUser().flatMapMany(uc ->
    //     fastApiClient.streamNewChat(userMessage, uc.userId(), uc.company()))
    //     .doOnNext(sse -> {
    //                  String ev = sse.event();
    //                 String data = sse.data();
    //                 if ("chunk".equals(ev)) {
    //                     System.out.println("[GW->FE] OUT event=chunk dataPreview=" + clip(data, 200));
    //                 } else {
    //                     System.out.println("[GW->FE] OUT event=" + ev + " len=" + (data != null ? data.length() : 0));
    //                 }
    //             })
    //             .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder()
    //                     .event("error")
    //                     .data(json(Map.of("type","error","content", Map.of("message", ex.getMessage()))))
    //                     .build()));
    //  }

    public Flux<ServerSentEvent<String>> continueChat(String chatId, String message) {
        return currentUser().flatMapMany(uc ->fastApiClient.streamChat(chatId, message, uc.userId(), uc.company()))
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