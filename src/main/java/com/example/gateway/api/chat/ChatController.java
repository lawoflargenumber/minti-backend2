package com.example.gateway.api.chat;

import com.example.gateway.api.dto.ChatDtos;
import com.example.gateway.application.chat.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/title")
    public Mono<ChatDtos.ChatTitleResponse> getTitle(@RequestParam String chatId) {
        return chatService.getTitle(chatId);
    }

    @GetMapping("")
    public Mono<ChatDtos.ChatHistoryResponse> getHistory(@RequestParam String chatId) {
        return chatService.getHistory(chatId);
    }

    @DeleteMapping("/{chatId}")
    public Mono<Void> delete(@PathVariable String chatId) {
        return chatService.deleteChat(chatId);
    }

    @PostMapping(path="/new", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<ChatDtos.NewChatResponse> newChat() {
        return chatService.newChat();
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> continueChat(@Valid @RequestBody ChatDtos.ContinueChatRequest req) {
        return chatService.continueChat(req.chatId, req.message);
    }
}