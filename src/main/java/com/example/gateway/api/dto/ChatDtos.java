package com.example.gateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class ChatDtos {

    public static class ChatIdRequest {
        @NotBlank
        public String chatId;
    }

    public static class ChatTitleResponse {
        public String title;
    }

    public static class ChatGetRequest {
        @NotBlank
        public String chatId;
    }

    public static class Message {
        public String speaker;
        public String content;
    }

    public static class ChatHistoryResponse {
        public String chatId;
        public List<Message> messages;
    }

    public static class NewChatRequest {
        @NotBlank
        public String message; // note: spec typo preserved
    }

    public static class ContinueChatRequest {
        @NotBlank
        public String chatId;
        @NotBlank
        public String message;
    }
}