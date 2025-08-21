package com.example.gateway.infra.fastapi;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(WebClient fastApiWebClient) {
        this.webClient = fastApiWebClient;
    }

    public Flux<ServerSentEvent<String>> streamNewChat(String userMessage, String userId, String company) {
        Map<String, Object> payload = Map.of(
            "userMessage", userMessage,
            "userId", userId,
            "company", company
        );
        return webClient.post()
                .uri("/chat/stream/new")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToFlux(ServerSentEvent.class)
                .map(sse -> ServerSentEvent.<String>builder()
                        .event(sse.event())
                        .id(sse.id())
                        .data(String.valueOf(sse.data()))
                        .build()
                );
    }

    public Flux<ServerSentEvent<String>> streamChat(String chatId, String userMessage, String userId, String company) {
        Map<String, Object> payload = Map.of(
            "chatId", chatId,
            "userMessage", userMessage,
            "userId", userId,
            "company", company
        );
        return webClient.post()
                .uri("/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToFlux(ServerSentEvent.class)
                .map(sse -> ServerSentEvent.<String>builder()
                        .event(sse.event())
                        .id(sse.id())
                        .data(String.valueOf(sse.data()))
                        .build()
                );
    }

    public Mono<Map> createPlanFromChat(String chatId) {
        Map<String, Object> payload = Map.of("chatId", chatId);
        return webClient.post()
                .uri("/chat/createPlan")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(Map.class);
    }

    public Mono<Map> createDesign(Map<String, Object> designPayload) {
        return webClient.post()
                .uri("/design")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(designPayload))
                .retrieve()
                .bodyToMono(Map.class);
    }
}