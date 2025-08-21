package com.example.gateway.application.auth;

import com.example.gateway.api.dto.AuthDtos;
import com.example.gateway.infra.mongo.ChatRepository;
import com.example.gateway.infra.mongo.PlanRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class AuthServiceStub {

    private final ChatRepository chatRepository;
    private final PlanRepository planRepository;

    public AuthServiceStub(ChatRepository chatRepository, PlanRepository planRepository) {
        this.chatRepository = chatRepository;
        this.planRepository = planRepository;
    }

    // Stub: authenticationCode를 단순 검증하고 user profile을 리턴.
    public Mono<AuthDtos.AuthResponse> authenticate(String authenticationCode) {
        if (StringUtils.isBlank(authenticationCode)) {
            return Mono.error(new IllegalArgumentException("authenticationCode is blank"));
        }
        // 테스트용 사용자 (고정)
        String userId = "test-user-id";
        String userName = "Test User";
        String company = "Test Company";
        String email = "test@example.com";

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
            var resp = new AuthDtos.AuthResponse();
            resp.userId = userId;
            resp.userName = userName;
            resp.company = company;
            resp.email = email;
            resp.chatList = t.getT1();
            resp.planList = t.getT2();
            return resp;
        });
    }
}