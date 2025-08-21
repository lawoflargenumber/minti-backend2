package com.example.gateway.application.auth;

import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.infra.mongo.ChatRepository;
import com.example.gateway.infra.mongo.PlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthServiceAzure {

    private final String tenantId;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final String tokenUri;

    private final WebClient webClient;
    private final ObjectMapper om = new ObjectMapper();

    private final JwtService jwtService;
    private final ChatRepository chatRepository;
    private final PlanRepository planRepository;

    public AuthServiceAzure(
            @Value("${app.auth.azure.tenant-id}") String tenantId,
            @Value("${app.auth.azure.client-id}") String clientId,
            @Value("${app.auth.azure.client-secret}") String clientSecret,
            @Value("${app.auth.azure.redirect-uri}") String redirectUri,
            @Value("${app.auth.azure.token-uri}") String tokenUri,
            WebClient.Builder webClientBuilder,
            JwtService jwtService,
            ChatRepository chatRepository,
            PlanRepository planRepository
    ) {
        this.tenantId = tenantId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.tokenUri = tokenUri;
        this.webClient = webClientBuilder.build();
        this.jwtService = jwtService;
        this.chatRepository = chatRepository;
        this.planRepository = planRepository;
    }

    public Mono<AuthResult> authenticateAndIssueJwt(String authenticationCode, String codeVerifierIfAny) {
        if (StringUtils.isBlank(authenticationCode)) {
            return Mono.error(new IllegalArgumentException("authenticationCode is blank"));
        }

        var form = BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
                .with("code", authenticationCode)
                .with("redirect_uri", redirectUri);
        if (StringUtils.isNotBlank(codeVerifierIfAny)) {
            form = BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", authenticationCode)
                    .with("redirect_uri", redirectUri)
                    .with("code_verifier", codeVerifierIfAny);
        }

        Mono<JsonNode> tokenResp = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(body -> {
                    try {
                        return Mono.just(om.readTree(body));
                    } catch (Exception e) {
                        return Mono.error(new IllegalStateException("Failed to parse token response", e));
                    }
                });

        return tokenResp.flatMap(json -> {
            String idToken = optText(json, "id_token");
            if (StringUtils.isBlank(idToken)) {
                return Mono.error(new IllegalStateException("id_token missing in token response"));
            }

            Map<String, Object> claims = decodeJwtPayload(idToken);
            String oid = asText(claims, "oid");
            if (StringUtils.isBlank(oid)) oid = asText(claims, "sub");
            if (StringUtils.isBlank(oid)) {
                return Mono.error(new IllegalStateException("oid/sub missing in id_token"));
            }

            String email = firstNonBlank(
                    asText(claims, "preferred_username"),
                    asText(claims, "email"),
                    asText(claims, "upn")
            );

            String company = firstNonBlank(
                    asText(claims, "extension_company"),
                    asText(claims, "tid"),
                    "Unknown"
            );

            String userName = firstNonBlank(
                    asText(claims, "name"),
                    email != null ? email : oid
            );

            String internalUserId = oid;

            Map<String, Object> extra = new HashMap<>();
            if (email != null) extra.put("email", email);
            if (company != null) extra.put("company", company);
            extra.put("oid", oid);

            String issuedJwt = jwtService.issueToken(internalUserId, extra);

            var chatsMono = chatRepository.findByUserIdOrderByUpdatedAtDesc(internalUserId).collectList()
                    .map(list -> list.stream().map(c -> {
                        var s = new AuthDtos.ChatSummary();
                        s.chatId = c.getChatId();
                        s.title = c.getTitle();
                        return s;
                    }).collect(Collectors.toList()));

            var plansMono = planRepository.findByUserIdOrderByCreatedAtDesc(internalUserId).collectList()
                    .map(list -> list.stream().map(p -> {
                        var s = new AuthDtos.PlanSummary();
                        s.planId = p.getPlanId();
                        s.title = p.getTitle();
                        return s;
                    }).collect(Collectors.toList()));

            return Mono.zip(chatsMono, plansMono).map(t -> {
                var body = new AuthDtos.AuthResponse();
                body.userId = internalUserId;
                body.userName = userName;
                body.company = company;
                body.email = email;
                body.chatList = t.getT1();
                body.planList = t.getT2();

                var result = new AuthResult();
                result.body = body;
                result.jwt = issuedJwt; 
                return result;
            });
        });
    }

    private static String optText(JsonNode n, String field) {
        return (n != null && n.has(field) && !n.get(field).isNull()) ? n.get(field).asText() : null;
    }

    private static String asText(Map<String, Object> map, String key) {
        Object v = map.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static String firstNonBlank(String... arr) {
        if (arr == null) return null;
        for (String s : arr) {
            if (StringUtils.isNotBlank(s)) return s;
        }
        return null;
    }

    private Map<String, Object> decodeJwtPayload(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) throw new IllegalArgumentException("invalid jwt");
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            JsonNode node = om.readTree(new String(payload, StandardCharsets.UTF_8));
            Map<String, Object> map = new HashMap<>();
            node.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().isNull() ? null : e.getValue().asText()));
            return map;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decode id_token payload", e);
        }
    }

    public static class AuthResult {
        public AuthDtos.AuthResponse body;
        public String jwt;
    }
}
