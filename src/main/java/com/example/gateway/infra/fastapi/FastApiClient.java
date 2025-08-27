package com.example.gateway.infra.fastapi;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class FastApiClient {

    private final WebClient webClient;

    public FastApiClient(WebClient fastApiWebClient) {
        this.webClient = fastApiWebClient;
    }

    public Mono<Map> newChat(String userId, String company, String message) {
        Map<String, Object> payload = Map.of(
            "userId", userId,
            "company", company,
            "message", message
        );
        return webClient.post()
                .uri("/chat/new")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(payload))
                .retrieve()
                .bodyToMono(Map.class);
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
                        .build())
                        .doOnSubscribe(sub ->
                            System.out.println("[FA->GW] /chat/stream/new SUBSCRIBE userId=" + userId + " company=" + company))
                    .doOnNext(sse ->
                            logSseInbound("/chat/stream/new", sse))
                    .doOnError(err ->
                            System.out.println("[FA->GW] /chat/stream/new ERROR: " + err))
                    .doFinally(sig ->
                            System.out.println("[FA->GW] /chat/stream/new FINALLY: " + sig));
                    
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
                .bodyToFlux(String.class)  // 원본 문자열로 받기
                .flatMap(this::parseSSELine)  // 수동 파싱
                .filter(sse -> sse != null)
                .doOnSubscribe(sub ->
                        System.out.println("[FA->GW] /chat/stream SUBSCRIBE chatId=" + chatId + " userId=" + userId))
                .doOnNext(sse ->
                        logSseInbound("/chat/stream", sse))
                .doOnError(err ->
                        System.out.println("[FA->GW] /chat/stream ERROR: " + err))
                .doFinally(sig ->
                        System.out.println("[FA->GW] /chat/stream FINALLY: " + sig));
    }

    public Mono<Map> createPlanFromChat(String chatId, String userId, String company) {
        Map<String, Object> payload = Map.of(
            "chatId", chatId,
            "userId", userId,
            "company", company
        );
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

    private void logSseInbound(String path, ServerSentEvent<String> sse) {
        String event = sse.event();
        String data = sse.data();
        if ("chunk".equals(event)) {
            System.out.println("[FA->GW] " + path + " IN event=chunk dataPreview=" + clip(data, 200));
        } else {
            System.out.println("[FA->GW] " + path + " IN event=" + event + " len=" + (data != null ? data.length() : 0));
        }
    }

    private static String clip(String s, int max) {
        if (s == null) return "";
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= max) return s;
        int cut = max;
        while (cut > 0 && (bytes[cut] & 0xC0) == 0x80) cut--; // 멀티바이트 안전
        return new String(bytes, 0, cut, StandardCharsets.UTF_8) + "...";
    }

    private Flux<ServerSentEvent<String>> parseSSELine(String line) {
        try {
            if (line.startsWith("data: ")) {
                String dataJson = line.substring(6);  // "data: " 제거
                if (dataJson.equals("[DONE]")) {
                    return Flux.empty();  // 스트림 종료 신호
                }
                
                String evt = extractTypeOrDefault(dataJson, null);
                return Flux.just(ServerSentEvent.<String>builder()
                        .event(evt)
                        .data(dataJson)  // 원본 JSON 그대로 전달
                        .build());
            }
        } catch (Exception e) {
            System.err.println("SSE 파싱 오류: " + e.getMessage() + ", line: " + line);
        }
        return Flux.empty();
    }

    private static String extractTypeOrDefault(String dataJson, String fallbackEvent) {
        try {
            if (dataJson != null && dataJson.startsWith("{")) {
                // 최소 파싱 (Jackson 써도 되고, 간단히 정규식 사용해도 됩니다)
                com.fasterxml.jackson.databind.JsonNode n =
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(dataJson);
                if (n.has("type") && !n.get("type").isNull()) {
                    String t = n.get("type").asText();
                    if (!t.isEmpty()) return t;          // type을 이벤트명으로 사용
                }
            }
        } catch (Exception ignore) {}
        // fallback: 기존 event가 있으면 그걸, 없으면 "message"
        return (fallbackEvent != null) ? fallbackEvent : "message";
    }
}