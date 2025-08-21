package com.example.gateway.api.auth;

import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.application.auth.AuthServiceAzure;
import com.example.gateway.application.auth.AuthServiceAzure.AuthResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceAzure authService; // ✅ Azure 연동 서비스 사용

    public AuthController(AuthServiceAzure authService) {
        this.authService = authService;
    }

    @PostMapping
    public Mono<ResponseEntity<AuthDtos.AuthResponse>> auth(@Valid @RequestBody AuthDtos.AuthRequest req) {

        return authService.authenticateAndIssueJwt(req.authenticationCode, null)
                .map(this::toResponseEntityWithAuthHeader);
    }

    private ResponseEntity<AuthDtos.AuthResponse> toResponseEntityWithAuthHeader(AuthResult result) {
        return ResponseEntity
                .ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + result.jwt)
                .header("Access-Control-Expose-Headers", HttpHeaders.AUTHORIZATION)
                .body(result.body);
    }
}
