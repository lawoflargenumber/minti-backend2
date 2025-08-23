package com.example.gateway.application.auth;

import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.infra.mongo.ChatRepository;
import com.example.gateway.infra.mongo.PlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceAzure.class);
    
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
        logger.info("🔍 인증 코드 검증 시작");
        
        if (StringUtils.isBlank(authenticationCode)) {
            logger.error("❌ 인증 코드가 비어있음");
            return Mono.error(new IllegalArgumentException("authenticationCode is blank"));
        }

        logger.info("📝 Azure 토큰 요청 폼 데이터 생성");
        var form = BodyInserters
                .fromFormData("grant_type", "authorization_code")
                .with("client_id", clientId)
                .with("client_secret", clientSecret)
                .with("code", authenticationCode)
                .with("redirect_uri", redirectUri);
        if (StringUtils.isNotBlank(codeVerifierIfAny)) {
            logger.info("🔐 PKCE code_verifier 추가");
            form = BodyInserters.fromFormData("grant_type", "authorization_code")
                    .with("client_id", clientId)
                    .with("client_secret", clientSecret)
                    .with("code", authenticationCode)
                    .with("redirect_uri", redirectUri)
                    .with("code_verifier", codeVerifierIfAny);
        }

        logger.info("🌐 Azure 토큰 엔드포인트 호출 시작 - URI: {}", tokenUri);
        Mono<JsonNode> tokenResp = webClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form)
                .exchangeToMono(res -> {
                    if (res.statusCode().is2xxSuccessful()) {
                        return res.bodyToMono(String.class);
                    } else {
                        return res.bodyToMono(String.class).defaultIfEmpty("")
                            .flatMap(body -> {
                                logger.error("❌ Azure token error [{}] body={}", res.statusCode(), body);
                                return Mono.error(new IllegalStateException("Azure token error: " + res.statusCode()));
                            });
                    }
                })
                .flatMap(body -> Mono.fromCallable(() -> om.readTree(body)));
            

        return tokenResp.flatMap(json -> {
            logger.info("🔍 ID 토큰 추출");
            String idToken = optText(json, "id_token");
            logger.info("🔍 ID 토큰: {}", idToken);
            if (StringUtils.isBlank(idToken)) {
                logger.error("❌ ID 토큰이 응답에 없음");
                return Mono.error(new IllegalStateException("id_token missing in token response"));
            }

            logger.info("🔓 ID 토큰 페이로드 디코딩");
            Map<String, Object> claims = decodeJwtPayload(idToken);
            String oid = asText(claims, "oid");
            if (StringUtils.isBlank(oid)) oid = asText(claims, "sub");
            if (StringUtils.isBlank(oid)) {
                logger.error("❌ ID 토큰에서 oid/sub 정보 누락");
                return Mono.error(new IllegalStateException("oid/sub missing in id_token"));
            }
            logger.info("👤 사용자 ID 추출 완료 - oid: {}", oid);

            logger.info("📧 사용자 이메일 정보 추출");
            String email = firstNonBlank(
                    asText(claims, "emailAddress"),
                    asText(claims, "email"),
                    asText(claims, "upn")
            );
            logger.info("📧 사용자 이메일: {}", email);

            logger.info("🏢 회사 정보 추출");
            String company = firstNonBlank(
                    asText(claims, "company"),
                    "Unknown"
            );
            logger.info("🏢 회사: {}", company);

            logger.info("👤 사용자 이름 추출");
            String userName = firstNonBlank(
                    asText(claims, "name"),
                    asText(claims, "given_name"),
                    email != null ? email : oid
            );
            logger.info("👤 사용자 이름: {}", userName);

            String internalUserId = oid;

            logger.info("🔑 JWT 토큰 발급 시작");
            Map<String, Object> extra = new HashMap<>();
            if (email != null) extra.put("email", email);
            if (company != null) extra.put("company", company);
            extra.put("oid", oid);

            String issuedJwt = jwtService.issueToken(internalUserId, extra);
            logger.info("🔑 JWT 토큰 발급 완료");

            logger.info("💬 사용자 채팅 목록 조회");
            var chatsMono = chatRepository.findByUserIdOrderByUpdatedAtDesc(internalUserId).collectList()
                    .doOnSuccess(chats -> logger.info("💬 채팅 목록 조회 완료 - 개수: {}", chats.size()))
                    .map(list -> list.stream().map(c -> {
                        var s = new AuthDtos.ChatSummary();
                        s.chatId = c.getChatId();
                        s.title = c.getTitle();
                        return s;
                    }).collect(Collectors.toList()));

            logger.info("📋 사용자 계획 목록 조회");
            var plansMono = planRepository.findByUserIdOrderByCreatedAtDesc(internalUserId).collectList()
                    .doOnSuccess(plans -> logger.info("📋 계획 목록 조회 완료 - 개수: {}", plans.size()))
                    .map(list -> list.stream().map(p -> {
                        var s = new AuthDtos.PlanSummary();
                        s.planId = p.getPlanId();
                        s.title = p.getTitle();
                        return s;
                    }).collect(Collectors.toList()));

            return Mono.zip(chatsMono, plansMono).map(t -> {
                logger.info("📦 인증 응답 데이터 구성");
                var body = new AuthDtos.AuthResponse();
                body.userId = internalUserId;
                body.token = issuedJwt;
                body.userName = userName;
                body.company = company;
                body.email = email;
                body.chatList = t.getT1();
                body.planList = t.getT2();

                var result = new AuthResult();
                result.body = body;
                result.jwt = issuedJwt; 
                logger.info("✅ 인증 처리 완료 - userId: {}, userName: {}", internalUserId, userName);
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

    public Mono<AuthDtos.AuthResponse> getUser(String authorization) {
        if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
            return Mono.error(new IllegalArgumentException("Invalid authorization header"));
        }
        
        String token = authorization.substring(7);
        
        return Mono.fromCallable(() -> jwtService.parse(token))
        .flatMap(claims -> {
            String userId = claims.get("sub", String.class);
            if (StringUtils.isBlank(userId)) {
                return Mono.error(new IllegalArgumentException("Invalid token: missing sub"));
            }
            
            String email = claims.get("email", String.class);
            String company = claims.get("company", String.class);
            String oid = claims.get("oid", String.class);
            
            String userName = StringUtils.isNotBlank(email) ? email : oid;
            
            var chatsMono = chatRepository.findByUserIdOrderByUpdatedAtDesc(userId).collectList()
                .map(list -> list.stream().map(c -> {
                    var s = new AuthDtos.ChatSummary();
                    s.chatId = c.getChatId();
                    s.title = c.getTitle();
                    return s;
                }).collect(Collectors.toList()));
            
            var plansMono = planRepository.findByUserIdOrderByCreatedAtDesc(userId).collectList()
                .map(list -> list.stream().map(p -> {
                    var s = new AuthDtos.PlanSummary();
                    s.planId = p.getPlanId();
                    s.title = p.getTitle();
                    return s;
                }).collect(Collectors.toList()));
            
            return Mono.zip(chatsMono, plansMono).map(t -> {
                var response = new AuthDtos.AuthResponse();
                response.userId = userId;
                response.token = token;
                response.userName = userName;
                response.company = company;
                response.email = email;
                response.chatList = t.getT1();
                response.planList = t.getT2();
                return response;
            });
        });
    }
}