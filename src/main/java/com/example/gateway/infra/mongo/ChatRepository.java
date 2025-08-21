package com.example.gateway.infra.mongo;

import com.example.gateway.domain.chat.Chat;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
    Mono<Chat> findByChat_id(String chatId);
    Mono<Void> deleteByChat_id(String chatId);
    Flux<Chat> findByUserIdOrderByUpdatedAtDesc(String userId);
}