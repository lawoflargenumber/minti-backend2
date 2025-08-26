package com.example.gateway.application.design;

import com.example.gateway.api.dto.DesignDtos.DesignCreateResponse;
import com.example.gateway.api.dto.DesignDtos.DesignCreateBrandRequest;
import com.example.gateway.api.dto.DesignDtos.DesignCreateCategoryRequest;
import com.example.gateway.infra.mongo.PlanRepository;  
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DesignService {
    
    private final PlanRepository planRepository;

    public DesignService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Mono<DesignCreateResponse> createType1(DesignCreateBrandRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                Map<String, Object> designData = new HashMap<>();
                ObjectMapper objectMapper = new ObjectMapper();
                
                try {
                    Map<String, Object> allFields = objectMapper.convertValue(req, Map.class);
                    allFields.remove("planId");
                    designData = allFields;
                } catch (Exception e) {
                    return Mono.error(new RuntimeException("Failed to convert request to map", e));
                }

                String jsonContent;
                try {
                    jsonContent = objectMapper.writeValueAsString(designData);
                } catch (Exception e) {
                    return Mono.error(new RuntimeException("Failed to serialize design data", e));
                }

                plan.setPlanContent(jsonContent);
                plan.setUrl("https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6");
                return planRepository.save(plan);
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }

    public Mono<DesignCreateResponse> createType2(DesignCreateCategoryRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                Map<String, Object> designData = new HashMap<>();
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    Map<String, Object> allFields = objectMapper.convertValue(req, Map.class);
                    allFields.remove("planId");
                    designData = allFields;
                } catch (Exception e) {
                    return Mono.error(new RuntimeException("Failed to convert request to map", e));
                }

                String jsonContent;
                try {
                    jsonContent = objectMapper.writeValueAsString(designData);
                } catch (Exception e) {
                    return Mono.error(new RuntimeException("Failed to serialize design data", e));
                }

                plan.setPlanContent(jsonContent);
                plan.setUrl("https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6");
                return planRepository.save(plan);
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }
}
