package com.example.gateway.application.sidebar;

import com.example.gateway.api.dto.SidebarDtos;
import com.example.gateway.infra.mongo.ChatRepository;
import com.example.gateway.infra.mongo.PlanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
public class SidebarService {

    private final ChatRepository chatRepository;
    private final PlanRepository planRepository;

    public SidebarService(ChatRepository chatRepository, PlanRepository planRepository) {
        this.chatRepository = chatRepository;
        this.planRepository = planRepository;
    }

    public Mono<SidebarDtos.SidebarResponse> getSidebar(String userId) {
        var chatsMono = chatRepository.findByUserIdOrderByUpdatedAtDesc(userId).collectList()
                .map(list -> list.stream().map(c -> {
                    var s = new SidebarDtos.ChatSummary();
                    s.chatId = c.getChatId();
                    s.title = c.getTitle();
                    return s;
                }).collect(Collectors.toList()));
        var plansMono = planRepository.findByUserIdOrderByCreatedAtDesc(userId).collectList()
                .map(list -> list.stream().map(p -> {
                    var s = new SidebarDtos.PlanSummary();
                    s.planId = p.getPlanId();
                    s.title = p.getTitle();
                    return s;
                }).collect(Collectors.toList()));

        return Mono.zip(chatsMono, plansMono)
                .map(t -> {
                    var resp = new SidebarDtos.SidebarResponse();
                    resp.chatList = t.getT1();
                    resp.planList = t.getT2();
                    return resp;
                });
    }
}