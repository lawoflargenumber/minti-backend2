package com.example.gateway.application.plan;

import com.example.gateway.api.dto.PlanDtos;
import com.example.gateway.domain.plan.Plan;
import com.example.gateway.infra.fastapi.FastApiClient;
import com.example.gateway.infra.mongo.PlanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import static com.example.gateway.application.ids.Ids.newId;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final FastApiClient fastApiClient;

    public PlanService(PlanRepository planRepository, FastApiClient fastApiClient) {
        this.planRepository = planRepository;
        this.fastApiClient = fastApiClient;
    }

    public Mono<PlanDtos.NewPlanResponse> newPlan(String type, String company, String userId) {
        Plan plan = new Plan();
        plan.setPlanId(UUID.randomUUID().toString().replace("-", "")); // UUID4 without hyphens
        plan.setTargetType(type);
        plan.setCompany(company);
        plan.setUserId(userId);
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setLastUpdatedAt(OffsetDateTime.now());
        plan.setPlanContent("{}"); // 빈 JSON
        plan.setShare(false); // 기본값 false
        plan.setTitle("Untitled"); // 기본 title
        
        return planRepository.save(plan)
                .map(p -> {
                    var resp = new PlanDtos.NewPlanResponse();
                    resp.planId = p.getPlanId();
                    resp.type = p.getTargetType();
                    return resp;
                });
    }

    public Mono<PlanDtos.GetDesignResponse> getDesign(String planId) {
        return planRepository.findByPlanId(planId)
                .map(p -> {
                    var resp = new PlanDtos.GetDesignResponse();
                    resp.targetType = p.getTargetType();
                    resp.createdAt = p.getCreatedAt();
                    resp.url = p.getUrl();
                    return resp;
                });
    }

    public Mono<?> getPlan(String planId) {
        return planRepository.findByPlanId(planId)
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                ObjectMapper objectMapper = new ObjectMapper();
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parsed = objectMapper.readValue(plan.getPlanContent(), Map.class);
                    return Mono.just(parsed);
                } catch (Exception e) {
                    return Mono.error(new RuntimeException("Failed to parse plan content", e));
                }
            });
    }

    public Mono<Map<String, Object>> savePlanFromChat(Map<String, Object> planData, String userId, String company) {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // FastAPI 응답에서 필요한 정보 추출
        String planId = UUID.randomUUID().toString().replace("-", "");
        String targetType = (String) planData.getOrDefault("type", "brand"); 
        String title = (String) planData.getOrDefault("title", "Untitled");
        
        // PlanContent를 JSON 문자열로 변환
        String planContent;
        try {
            planContent = objectMapper.writeValueAsString(planData);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to serialize plan data", e));
        }
        
        // Plan 객체 생성 및 저장
        Plan plan = new Plan();
        plan.setPlanId(planId);
        plan.setTargetType(targetType);
        plan.setCompany(company);
        plan.setUserId(userId);
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setLastUpdatedAt(OffsetDateTime.now());
        plan.setPlanContent(planContent);
        plan.setShare(false);
        plan.setTitle(title);
        
        return planRepository.save(plan)
                .map(p -> {
                    // FastAPI 응답에 planId만 추가
                    Map<String, Object> response = new HashMap<>(planData);
                    response.put("planId", p.getPlanId());
                    return response;
                });
    }
}