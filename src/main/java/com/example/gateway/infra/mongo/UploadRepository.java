package com.example.gateway.infra.mongo;

import com.example.gateway.domain.upload.Upload;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface UploadRepository extends ReactiveMongoRepository<Upload, String> {
    Flux<Upload> findByPlanId(String planId);
}