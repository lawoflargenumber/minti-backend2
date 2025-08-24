package com.example.gateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;

public class ChatDtos {

    public static class ChatIdRequest {
        @NotBlank
        public String chatId;
    }

    public static class ChatTitleResponse {
        public String title;
    }

    public static class ChatGetRequest {
        public String chatId;
    }

    public static class Message {
        public String speaker;
        public String content;
        public JsonNode graph;
        public String plan;
    }

    public static class ChatHistoryResponse {
        public String chatId;
        public List<Message> messages;
    }

    public static class NewChatRequest {
        @NotBlank
        public String message; 
    }

    public static class ContinueChatRequest {
        @NotBlank
        public String chatId;
        @NotBlank
        public String message;
    }
}