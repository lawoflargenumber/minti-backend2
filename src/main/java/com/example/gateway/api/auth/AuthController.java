package com.example.gateway.api.auth;

import com.azure.core.annotation.Get;
import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.application.auth.AuthServiceAzure;
import com.example.gateway.application.auth.AuthServiceAzure.AuthResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthServiceAzure authService; 

    public AuthController(AuthServiceAzure authService) {
        this.authService = authService;
    }

    @PostMapping
    public Mono<ResponseEntity<AuthDtos.AuthResponse>> auth(@Valid @RequestBody AuthDtos.AuthRequest req) {
        logger.info("🔐 로그인 요청 수신 - authenticationCode: {}", req.authenticationCode != null ? req.authenticationCode.substring(0, Math.min(10, req.authenticationCode.length())) + "..." : "null");
        
        return authService.authenticateAndIssueJwt(req.authenticationCode, null)
                .doOnSuccess(result -> {
                    logger.info("✅ 로그인 성공 - userId: {}, userName: {}, company: {}", 
                        result.body.userId, result.body.userName, result.body.company);
                })
                .doOnError(error -> {
                    logger.error("❌ 로그인 실패 - error: {}", error.getMessage());
                })
                .map(this::toResponseEntityWithAuthHeader);
    }

    private ResponseEntity<AuthDtos.AuthResponse> toResponseEntityWithAuthHeader(AuthResult result) {
        logger.info("📤 로그인 응답 생성 - JWT 토큰 발급 완료");
        return ResponseEntity
                .ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.jwt)
                .header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)
                .body(result.body);
    }

    @GetMapping("/user")
    public Mono<AuthDtos.AuthResponse> getUser(@RequestHeader("Authorization") String authorization) {
        return authService.getUser(authorization);
    }
}
