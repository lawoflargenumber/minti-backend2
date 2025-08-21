package com.example.gateway.application.plan;

import com.example.gateway.api.dto.PlanDtos;
import com.example.gateway.domain.plan.Plan;
import com.example.gateway.infra.fastapi.FastApiClient;
import com.example.gateway.infra.mongo.PlanRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;

import static com.example.gateway.application.ids.Ids.newId;

@Service
public class PlanService {

    private final PlanRepository planRepository;
    private final FastApiClient fastApiClient;

    public PlanService(PlanRepository planRepository, FastApiClient fastApiClient) {
        this.planRepository = planRepository;
        this.fastApiClient = fastApiClient;
    }

    public Mono<PlanDtos.NewPlanResponse> newPlan() {
        Plan plan = new Plan();
        plan.setPlanId(newId());
        plan.setUserId("test-user-id");
        plan.setTargetType("brand");
        plan.setCreatedAt(OffsetDateTime.now());
        plan.setTitle("Untitled Plan");
        return planRepository.save(plan)
                .map(p -> {
                    var resp = new PlanDtos.NewPlanResponse();
                    resp.planId = p.getPlanId();
                    resp.type = p.getTargetType();
                    return resp;
                });
    }

    public Mono<Object> createPlanFromChat(String chatId) {
        return fastApiClient.createPlanFromChat(chatId)
                .flatMap(map -> {
                    // persist to Mongo as Plan
                    Plan plan = new Plan();
                    plan.setPlanId((String) map.get("planId"));
                    plan.setUserId("test-user-id");
                    // Determine targetType by presence of fields
                    String targetType = map.containsKey("couponSection") || map.containsKey("productSection") ? "brand" : "category";
                    plan.setTargetType(targetType);
                    plan.setTitle((String) map.get("title"));
                    plan.setMainBanner((String) map.get("mainBanner"));
                    if ("brand".equals(targetType)) {
                        plan.setCouponSection((String) map.get("couponSection"));
                        plan.setProductSection((String) map.get("productSection"));
                        plan.setEventNotes((String) map.get("eventNotes"));
                    } else {
                        plan.setSection1((String) map.get("section1"));
                        plan.setSection2((String) map.get("section2"));
                        plan.setSection3((String) map.get("section3"));
                    }
                    plan.setCreatedAt(OffsetDateTime.now());
                    return planRepository.save(plan).thenReturn(map);
                });
    }

    public Mono<PlanDtos.DesignResponse> createDesign(Map<String, Object> designPayload) {
        return fastApiClient.createDesign(designPayload)
                .flatMap(map -> {
                    String planId = (String) map.get("planId");
                    String designUrl = (String) map.get("designUrl");
                    return planRepository.findByPlanId(planId)
                            .flatMap(p -> {
                                p.setDesignUrl(designUrl);
                                return planRepository.save(p);
                            })
                            .then(Mono.fromSupplier(() -> {
                                var resp = new PlanDtos.DesignResponse();
                                resp.planId = planId;
                                resp.designUrl = designUrl;
                                return resp;
                            }));
                });
    }
}