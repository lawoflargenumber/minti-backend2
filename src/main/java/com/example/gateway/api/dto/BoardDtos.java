package com.example.gateway.api.dto;

import java.time.OffsetDateTime;

public class BoardDtos {

    public static class BoardItem {
        public String planId;
        public String title;
        public String url;
        public OffsetDateTime createdAt;
        public String targetType;
    }
}