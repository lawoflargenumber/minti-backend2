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

import static com.example.gateway.application.ids.Ids.newId;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final FastApiClient fastApiClient;

    public ChatService(ChatRepository chatRepository, FastApiClient fastApiClient) {
        this.chatRepository = chatRepository;
        this.fastApiClient = fastApiClient;
    }

    public Mono<ChatDtos.ChatTitleResponse> getTitle(String chatId) {
        return chatRepository.findByChat_id(chatId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("chatId not found")))
                .map(c -> {
                    var resp = new ChatDtos.ChatTitleResponse();
                    resp.title = c.getTitle();
                    return resp;
                });
    }

    public Mono<ChatDtos.ChatHistoryResponse> getHistory(String chatId) {
        return chatRepository.findByChat_id(chatId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("chatId not found")))
                .map(c -> {
                    var resp = new ChatDtos.ChatHistoryResponse();
                    resp.chatId = c.getChat_id();
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
        return chatRepository.deleteByChat_id(chatId);
    }

    public Flux<ServerSentEvent<String>> newChat(String userMessage) {
        String chatId = newId();
        Chat chat = new Chat();
        chat.setChat_id(chatId);
        chat.setUserId("test-user-id"); // stub
        chat.setTitle(userMessage.length() > 30 ? userMessage.substring(0,30) : userMessage);
        chat.setCreatedAt(OffsetDateTime.now());
        chat.setUpdatedAt(OffsetDateTime.now());
        chat.getMessages().add(new ChatMessage("user", userMessage, OffsetDateTime.now()));

        return chatRepository.save(chat)
                .thenMany(Flux.concat(
                    Flux.just(ServerSentEvent.<String>builder().event("chat_id").data("{"type":"chat_id","content":"" + chatId + ""}").build()),
                    fastApiClient.streamNewChat(userMessage, "test-user-id", "Test Company")
                ))
                .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder().event("error").data("{"type":"error","content":{"message":"" + ex.getMessage() + ""}}").build()));
    }

    public Flux<ServerSentEvent<String>> continueChat(String chatId, String message) {
        return chatRepository.findByChat_id(chatId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("chatId not found")))
                .flatMapMany(chat -> {
                    chat.getMessages().add(new ChatMessage("user", message, OffsetDateTime.now()));
                    chat.setUpdatedAt(OffsetDateTime.now());
                    return chatRepository.save(chat)
                            .thenMany(fastApiClient.streamChat(chat.getChat_id(), message, "test-user-id", "Test Company"));
                })
                .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder().event("error").data("{"type":"error","content":{"message":"" + ex.getMessage() + ""}}").build()));
    }
}