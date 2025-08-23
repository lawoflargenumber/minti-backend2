package com.example.gateway.domain.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.OffsetDateTime;

@Document(collection = "messages")
public class ChatMessage {
    @Id
    private String _id;
    
    @Field("chat_id")
    private String chatId;
    
    private String speaker; // "user" | "ai"
    private String content;
    
    @Field("timestamp")
    private OffsetDateTime timestamp;
    
    private OffsetDateTime ts;

    public ChatMessage() {}

    public ChatMessage(String speaker, String content, OffsetDateTime ts) {
        this.speaker = speaker;
        this.content = content;
        this.ts = ts;
    }
    
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getSpeaker() { return speaker; }
    public void setSpeaker(String speaker) { this.speaker = speaker; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(OffsetDateTime timestamp) { this.timestamp = timestamp; }
    public OffsetDateTime getTs() { return ts; }
    public void setTs(OffsetDateTime ts) { this.ts = ts; }
}