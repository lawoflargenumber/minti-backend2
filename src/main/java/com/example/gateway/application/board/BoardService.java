package com.example.gateway.application.board;

import com.example.gateway.api.dto.BoardDtos;
import com.example.gateway.infra.mongo.PlanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BoardService {

    private final PlanRepository planRepository;

    public BoardService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Flux<BoardDtos.BoardItem> all() {
        return planRepository.findAll()
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getDesignUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }

    public Flux<BoardDtos.BoardItem> brand() {
        return planRepository.findByTargetTypeOrderByCreatedAtDesc("brand")
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getDesignUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }

    public Flux<BoardDtos.BoardItem> category() {
        return planRepository.findByTargetTypeOrderByCreatedAtDesc("category")
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getDesignUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }
}