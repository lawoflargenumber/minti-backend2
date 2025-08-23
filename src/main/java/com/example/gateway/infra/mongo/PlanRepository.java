package com.example.gateway.infra.mongo;

import com.example.gateway.domain.plan.Plan;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlanRepository extends ReactiveMongoRepository<Plan, String> {
    Mono<Plan> findByPlanId(String planId);
    Flux<Plan> findByUserIdOrderByCreatedAtDesc(String userId);
    Flux<Plan> findByTargetTypeOrderByCreatedAtDesc(String targetType);
    Flux<Plan> findByCompanyAndUrlIsNotNullAndShareTrueOrderByCreatedAtDesc(String company);
    Flux<Plan> findByCompanyAndUrlIsNotNullAndShareTrueAndTargetTypeOrderByCreatedAtDesc(String company, String targetType);
}