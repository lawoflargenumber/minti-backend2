package com.example.gateway.api.plan;

import com.example.gateway.api.dto.PlanDtos;
import com.example.gateway.application.plan.PlanService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping("/chat/createPlan")
    public Mono<Object> createPlanFromChat(@Valid @RequestBody PlanDtos.CreatePlanFromChatRequest req) {
        return planService.createPlanFromChat(req.chatId);
    }

    @PostMapping("/plan/new")
    public Mono<PlanDtos.NewPlanResponse> newPlan() {
        return planService.newPlan();
    }

    @PostMapping("/design")
    public Mono<PlanDtos.DesignResponse> createDesign(@RequestBody Map<String, Object> payload) {
        return planService.createDesign(payload);
    }

    @PostMapping("/design/share")
    public Mono<Void> share(@Valid @RequestBody PlanDtos.ShareDesignRequest req) {
        // No-op for now
        return Mono.empty();
    }
}