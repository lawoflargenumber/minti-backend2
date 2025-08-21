package com.example.gateway.domain.chat;

import java.time.OffsetDateTime;

public class ChatMessage {
    private String speaker; // "user" | "ai"
    private String content;
    private OffsetDateTime ts;

    public ChatMessage() {}

    public ChatMessage(String speaker, String content, OffsetDateTime ts) {
        this.speaker = speaker;
        this.content = content;
        this.ts = ts;
    }
    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getTs() { return ts; }
    public void setTs(OffsetDateTime ts) { this.ts = ts; }
}