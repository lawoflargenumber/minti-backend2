package com.example.gateway.api.auth;

import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.application.auth.AuthServiceStub;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceStub authServiceStub;

    public AuthController(AuthServiceStub authServiceStub) {
        this.authServiceStub = authServiceStub;
    }

    @PostMapping
    public Mono<AuthDtos.AuthResponse> auth(@Valid @RequestBody AuthDtos.AuthRequest req) {
        return authServiceStub.authenticate(req.authenticationCode);
    }
}