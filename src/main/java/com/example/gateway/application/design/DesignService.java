package com.example.gateway.application.design;

import com.example.gateway.api.dto.DesignDtos.DesignCreateResponse;
import com.example.gateway.api.dto.DesignDtos.DesignCreateBrandRequest;
import com.example.gateway.api.dto.DesignDtos.DesignCreateCategoryRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class DesignService {

    public Mono<DesignCreateResponse> createType1(DesignCreateBrandRequest req) {
        return Mono.just(new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }

    public Mono<DesignCreateResponse> createType2(DesignCreateCategoryRequest req) {
        return Mono.just(new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }
}
