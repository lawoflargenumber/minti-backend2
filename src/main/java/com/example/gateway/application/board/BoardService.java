package com.example.gateway.application.board;

import com.example.gateway.api.dto.BoardDtos;
import com.example.gateway.application.auth.JwtService;
import com.example.gateway.infra.mongo.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BoardService {

    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);
    
    private final PlanRepository planRepository;
    private final JwtService jwtService;

    public BoardService(PlanRepository planRepository, JwtService jwtService) {
        this.planRepository = planRepository;
        this.jwtService = jwtService;
    }

    public Flux<BoardDtos.BoardItem> all(String token) {
        String company = jwtService.extractCompany(token);
        if (company == null) {
            return Flux.empty();
        }
        
        return planRepository.findByCompanyAndUrlIsNotNullAndShareTrueOrderByCreatedAtDesc(company)
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }

    public Flux<BoardDtos.BoardItem> brand(String token) {
        String company = jwtService.extractCompany(token);
        if (company == null) {
            return Flux.empty();
        }
        
        return planRepository.findByCompanyAndUrlIsNotNullAndShareTrueAndTargetTypeOrderByCreatedAtDesc(company, "brand")
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }

    public Flux<BoardDtos.BoardItem> category(String token) {
        String company = jwtService.extractCompany(token);
        if (company == null) {
            return Flux.empty();
        }
        
        return planRepository.findByCompanyAndUrlIsNotNullAndShareTrueAndTargetTypeOrderByCreatedAtDesc(company, "category")
                .map(p -> {
                    var b = new BoardDtos.BoardItem();
                    b.planId = p.getPlanId();
                    b.title = p.getTitle();
                    b.url = p.getUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }
}