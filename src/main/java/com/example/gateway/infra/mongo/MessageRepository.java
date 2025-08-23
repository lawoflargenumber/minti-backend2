package com.example.gateway.infra.mongo;

import com.example.gateway.domain.chat.ChatMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface MessageRepository extends ReactiveMongoRepository<ChatMessage, String> {
    // chat_id로 조회하고 timestamp로 정렬
    Flux<ChatMessage> findByChatIdOrderByTimestampAsc(String chatId);
    
    // messageIds로 조회하고 timestamp로 정렬
    Flux<ChatMessage> findBy_idInOrderByTimestampAsc(List<String> messageIds);
}
