package com.example.gateway.application.plan;

import com.example.gateway.api.dto.PlanDtos;
import com.example.gateway.domain.plan.Plan;
import com.example.gateway.infra.fastapi.FastApiClient;
import com.example.gateway.infra.mongo.PlanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    public Mono<PlanDtos.NewPlanResponse> newPlan(String type, String company) {
        Plan plan = new Plan();
        plan.setPlanId(UUID.randomUUID().toString().replace("-", "")); // UUID4 without hyphens
        plan.setTargetType(type);
        plan.setCompany(company);
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setLastUpdatedAt(OffsetDateTime.now());
        plan.setPlanContent("{}"); // 빈 JSON
        plan.setShare(false); // 기본값 false
        
        return planRepository.save(plan)
                .map(p -> {
                    var resp = new PlanDtos.NewPlanResponse();
                    resp.planId = p.getPlanId();
                    resp.type = p.getTargetType();
                    return resp;
                });
    }

    public Mono<PlanDtos.DesignResponse> createDesign(Map<String, Object> designPayload) {
        return fastApiClient.createDesign(designPayload)
                .flatMap(map -> {
                    String planId = (String) map.get("planId");
                    String designUrl = (String) map.get("designUrl");
                    return planRepository.findByPlanId(planId)
                            .flatMap(p -> {
                                // plan_content에 designUrl 정보를 추가하거나 별도 필드로 저장
                                // 현재는 단순히 응답만 반환
                                return Mono.empty();
                            })
                            .then(Mono.fromSupplier(() -> {
                                var resp = new PlanDtos.DesignResponse();
                                resp.planId = planId;
                                resp.designUrl = designUrl;
                                return resp;
                            }));
                });
    }

    public Mono<PlanDtos.PlanResponse> getPlan(String planId) {
        return planRepository.findByPlanId(planId)
                .map(p -> {
                    var resp = new PlanDtos.PlanResponse();
                    resp.targetType = p.getTargetType();
                    resp.createdAt = p.getCreatedAt();
                    resp.url = p.getUrl();
                    return resp;
                });
    }
}