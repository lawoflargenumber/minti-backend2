package com.example.gateway.application.board;

import com.example.gateway.api.dto.BoardDtos;
import com.example.gateway.application.auth.JwtService;
import com.example.gateway.infra.mongo.PlanRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class BoardService {

    private final PlanRepository planRepository;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    public BoardService(PlanRepository planRepository, JwtService jwtService, ObjectMapper objectMapper) {
        this.planRepository = planRepository;
        this.jwtService = jwtService;
        this.objectMapper = objectMapper;
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
                    b.title = extractTitleFromPlanContent(p.getPlanContent());
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
                    b.title = extractTitleFromPlanContent(p.getPlanContent());
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
                    b.title = extractTitleFromPlanContent(p.getPlanContent());
                    b.url = p.getUrl();
                    b.createdAt = p.getCreatedAt();
                    b.targetType = p.getTargetType();
                    return b;
                });
    }

    private String extractTitleFromPlanContent(String planContent) {
        try {
            if (planContent != null && !planContent.isEmpty()) {
                JsonNode jsonNode = objectMapper.readTree(planContent);
                if (jsonNode.has("title")) {
                    return jsonNode.get("title").asText();
                }
            }
        } catch (Exception e) {
            // JSON 파싱 실패 시 기본값 반환
        }
        return "Untitled Plan";
    }
}