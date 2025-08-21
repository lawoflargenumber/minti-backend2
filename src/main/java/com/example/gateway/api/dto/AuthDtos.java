package com.example.gateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class AuthDtos {

    public static class AuthRequest {
        @NotBlank
        public String authenticationCode;
    }

    public static class ChatSummary {
        public String chatId;
        public String title;
    }

    public static class PlanSummary {
        public String planId;
        public String title;
    }

    public static class AuthResponse {
        public String userId;
        public String userName;
        public String company;
        public String email;
        public List<ChatSummary> chatList;
        public List<PlanSummary> planList;
    }
}