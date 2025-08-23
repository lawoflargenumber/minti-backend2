package com.example.gateway.api.plan;

import com.example.gateway.api.dto.PlanDtos;
import com.example.gateway.application.auth.JwtService;
import com.example.gateway.application.plan.PlanService;
import com.example.gateway.infra.fastapi.FastApiClient;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping
public class PlanController {

    private final PlanService planService;
    private final FastApiClient fastApiClient;
    private final JwtService jwtService;

    public PlanController(PlanService planService, FastApiClient fastApiClient, JwtService jwtService) {
        this.planService = planService;
        this.fastApiClient = fastApiClient;
        this.jwtService = jwtService;
    }

    @PostMapping("/chat/createPlan")
    public Mono<Map> createPlanFromChat(@Valid @RequestBody PlanDtos.CreatePlanFromChatRequest req) {
        return fastApiClient.createPlanFromChat(req.chatId);
    }

    @PostMapping("/plan/new")
    public Mono<PlanDtos.NewPlanResponse> newPlan(@RequestParam String type, @RequestHeader("Authorization") String token) {
        String company = jwtService.extractCompany(token.replace("Bearer ", ""));
        return planService.newPlan(type, company);
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

    @GetMapping("/plan")
    public Mono<PlanDtos.PlanResponse> getPlan(@RequestParam String planId) {
        return planService.getPlan(planId);
    }
}