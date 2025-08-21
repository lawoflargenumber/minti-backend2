package com.example.gateway.api.dto;

import java.util.List;

public class SidebarDtos {
    public static class ChatSummary {
        public String chatId;
        public String title;
    }
    public static class PlanSummary {
        public String planId;
        public String title;
    }
    public static class SidebarResponse {
        public List<ChatSummary> chatList;
        public List<PlanSummary> planList;
    }
}