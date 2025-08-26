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
    public Mono<Map> createPlanFromChat(@Valid @RequestBody PlanDtos.CreatePlanFromChatRequest req, @RequestHeader("Authorization") String token) {
        String company = jwtService.extractCompany(token.replace("Bearer ", ""));
        String userId = jwtService.parse(token.replace("Bearer ", "")).get("oid", String.class);
        return fastApiClient.createPlanFromChat(req.chatId, userId, company);
    }

    @PostMapping("/plan/new")
    public Mono<PlanDtos.NewPlanResponse> newPlan(@Valid @RequestBody PlanDtos.NewPlanRequest req, @RequestHeader("Authorization") String token) {
        String company = jwtService.extractCompany(token.replace("Bearer ", ""));
        String userId = jwtService.parse(token.replace("Bearer ", "")).getSubject();
        return planService.newPlan(req.type, company, userId);
    }

    @PostMapping("/design/share")
    public Mono<Void> share(@Valid @RequestBody PlanDtos.ShareDesignRequest req) {
        // No-op for now
        return Mono.empty();
    }

    @GetMapping("/design")
    public Mono<PlanDtos.GetDesignResponse> getDesign(@RequestParam String planId) {
        return planService.getDesign(planId);
    }

    @GetMapping("/plan")
    public Mono<?> getPlan(@RequestParam String planId) {
        return planService.getPlan(planId);
    }
}