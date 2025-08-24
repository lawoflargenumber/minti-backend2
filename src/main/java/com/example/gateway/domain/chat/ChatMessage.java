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

    @Field("message_id")
    private String messageId;
    
    private String speaker;

    private String content;
    
    @Field("timestamp")
    private OffsetDateTime timestamp;
    
    @Field("graph_data")
    private String graph;

    private String plan;

    public ChatMessage() {}

    public ChatMessage(String speaker, String content, OffsetDateTime timestamp) {
        this.speaker = speaker;
        this.content = content;
        this.timestamp = timestamp;
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
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getGraph() { return graph; }
    public void setGraph(String graph) { this.graph = graph; }
    public String getPlan() { return plan; }    
    public void setPlan(String plan) { this.plan = plan; }
}