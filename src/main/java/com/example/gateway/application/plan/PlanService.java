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

    // public Mono<?> getPlan(String planId) {
    //     Plan plan = planRepository.findByPlanId(planId).block();
    //     if (plan == null) {
    //         return Mono.error(new RuntimeException("Plan not found"));
    //     }

    //     ObjectMapper objectMapper = new ObjectMapper();

    //     try {
    //         Map<String, Object> parsed = objectMapper.readValue(plan.getPlanContent(), Map.class);
    //         return Mono.just(parsed);
    //     } catch (Exception e) {
    //         return Mono.error(new RuntimeException("Failed to parse plan content", e));
    //     }
    // }
}