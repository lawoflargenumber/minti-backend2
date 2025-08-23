package com.example.gateway.domain.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "chats")
public class Chat {
    @Id
    private String _id; // internal
    
    @Field("chat_id")
    private String chatId; // external key
    
    @Field("user_id")
    private String userId;

    private String title;

    private List<String> messageIds;

    @Field("created_at")
    private OffsetDateTime createdAt;

    @Field("last_updated")
    private OffsetDateTime updatedAt;

    public Chat() {}

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getMessageIds() { return messageIds; }
    public void setMessageIds(List<String> messageIds) { this.messageIds = messageIds; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}