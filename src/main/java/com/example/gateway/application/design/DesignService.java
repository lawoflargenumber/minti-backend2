package com.example.gateway.application.design;

import com.example.gateway.api.dto.DesignDtos.DesignCreateResponse;
import com.example.gateway.api.dto.DesignDtos.DesignCreateBrandRequest;
import com.example.gateway.api.dto.DesignDtos.DesignCreateCategoryRequest;
import com.example.gateway.infra.mongo.PlanRepository;
import com.example.gateway.infra.mongo.UploadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DesignService {
    
    private static final Logger logger = LoggerFactory.getLogger(DesignService.class);
    
    private final PlanRepository planRepository;
    private final UploadRepository uploadRepository;
    private final WebClient webClient;

    public DesignService(PlanRepository planRepository, UploadRepository uploadRepository, WebClient webClient) {
        this.planRepository = planRepository;
        this.uploadRepository = uploadRepository;
        this.webClient = webClient;
    }

    public Mono<DesignCreateResponse> createType1(DesignCreateBrandRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                // 모든 섹션을 병렬로 생성
                return Mono.zip(
                    createSection("title", req.getTitle(), req.getTitleImages()),
                    createSection("main_banner", req.getMainBanner(), req.getMainBannerImages()),
                    createSection("coupon_section", req.getCouponSection(), req.getCouponSectionImages()),
                    createSection("product_section", req.getProductSection(), req.getProductSectionImages()),
                    createSection("event_notes", req.getEventNotes(), req.getEventNotesImages())
                )
                .flatMap(tuple -> {
                    // 새로운 API 형식으로 데이터 변환
                    Map<String, Object> apiPayload = new HashMap<>();
                    List<Map<String, Object>> sections = new ArrayList<>();
                    sections.add(tuple.getT1());
                    sections.add(tuple.getT2());
                    sections.add(tuple.getT3());
                    sections.add(tuple.getT4());
                    sections.add(tuple.getT5());
                    
                    apiPayload.put("sections", sections);
                    
                    // 요청 로깅
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String requestJson = objectMapper.writeValueAsString(apiPayload);
                        logger.info("=== Creazy API Request (createType1) ===");
                        logger.info("URL: https://creazy.app/api/external/generate-sections");
                        logger.info("Request Body: {}", requestJson);
                    } catch (Exception e) {
                        logger.error("Failed to log request: {}", e.getMessage());
                    }
                    
                    // Creazy API 호출하여 실제 디자인 생성
                    return webClient.post()
                        .uri("https://creazy.app/api/external/generate-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(apiPayload))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .doOnNext(designResponse -> {
                            // 응답 로깅
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                String responseJson = objectMapper.writeValueAsString(designResponse);
                                logger.info("=== Creazy API Response (createType1) ===");
                                logger.info("Response Body: {}", responseJson);
                            } catch (Exception e) {
                                logger.error("Failed to log response: {}", e.getMessage());
                            }
                        })
                        .flatMap(designResponse -> {
                            // 응답에서 deployment.url 추출
                            @SuppressWarnings("unchecked")
                            Map<String, Object> deployment = (Map<String, Object>) designResponse.get("deployment");
                            if (deployment == null) {
                                return Mono.error(new RuntimeException("Design API response does not contain deployment"));
                            }
                            
                            String designUrl = (String) deployment.get("url");
                            if (designUrl == null) {
                                return Mono.error(new RuntimeException("Design API did not return URL"));
                            }
                            
                            // Plan 업데이트
                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                String jsonContent = objectMapper.writeValueAsString(req);
                                plan.setTitle(req.getTitle());
                                plan.setPlanContent(jsonContent);
                                plan.setUrl(designUrl);
                                return planRepository.save(plan);
                            } catch (Exception e) {
                                return Mono.error(new RuntimeException("Failed to serialize plan content", e));
                            }
                        });
                });
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), savedPlan.getUrl()));
    }
    
    private Mono<Map<String, Object>> createSection(String name, String plan, List<String> imageIds) {
        Map<String, Object> section = new HashMap<>();
        section.put("name", name);
        section.put("plan", plan);
        
        // 첫 번째 이미지 ID로 Upload에서 URL 조회
        if (imageIds != null && !imageIds.isEmpty()) {
            String firstImageId = imageIds.get(0);
            return uploadRepository.findByImageId(firstImageId)
                .map(upload -> {
                    List<Map<String, String>> images = new ArrayList<>();
                    Map<String, String> image = new HashMap<>();
                    image.put("url", upload.getPath());
                    images.add(image);
                    section.put("images", images);
                    return section;
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Upload를 찾지 못한 경우 빈 이미지 리스트
                    List<Map<String, String>> images = new ArrayList<>();
                    section.put("images", images);
                    return Mono.just(section);
                }));
        } else {
            // 이미지가 없는 경우 빈 이미지 리스트
            List<Map<String, String>> images = new ArrayList<>();
            section.put("images", images);
            return Mono.just(section);
        }
    }

    public Mono<DesignCreateResponse> createType2(DesignCreateCategoryRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                // 모든 섹션을 병렬로 생성
                return Mono.zip(
                    createSection("title", req.getTitle(), req.getTitleImages()),
                    createSection("main_banner", req.getMainBanner(), req.getMainBannerImages()),
                    createSection("section1", req.getSection1(), req.getSection1Images()),
                    createSection("section2", req.getSection2(), req.getSection2Images()),
                    createSection("section3", req.getSection3(), req.getSection3Images())
                )
                .flatMap(tuple -> {
                    // 새로운 API 형식으로 데이터 변환
                    Map<String, Object> apiPayload = new HashMap<>();
                    List<Map<String, Object>> sections = new ArrayList<>();
                    sections.add(tuple.getT1());
                    sections.add(tuple.getT2());
                    sections.add(tuple.getT3());
                    sections.add(tuple.getT4());
                    sections.add(tuple.getT5());
                    
                    apiPayload.put("sections", sections);
                    
                    // 요청 로깅
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String requestJson = objectMapper.writeValueAsString(apiPayload);
                        logger.info("=== Creazy API Request (createType2) ===");
                        logger.info("URL: https://creazy.app/api/external/generate-sections");
                        logger.info("Request Body: {}", requestJson);
                    } catch (Exception e) {
                        logger.error("Failed to log request: {}", e.getMessage());
                    }
                    
                    // Creazy API 호출하여 실제 디자인 생성
                    return webClient.post()
                        .uri("https://creazy.app/api/external/generate-sections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(apiPayload))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .doOnNext(designResponse -> {
                            // 응답 로깅
                            try {
                                ObjectMapper objectMapper = new ObjectMapper();
                                String responseJson = objectMapper.writeValueAsString(designResponse);
                                logger.info("=== Creazy API Response (createType2) ===");
                                logger.info("Response Body: {}", responseJson);
                            } catch (Exception e) {
                                logger.error("Failed to log response: {}", e.getMessage());
                            }
                        })
                        .flatMap(designResponse -> {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> deployment = (Map<String, Object>) designResponse.get("deployment");
                            if (deployment == null) {
                                return Mono.error(new RuntimeException("Design API response does not contain deployment"));
                            }
                            
                            String designUrl = (String) deployment.get("url");
                            if (designUrl == null) {
                                return Mono.error(new RuntimeException("Design API did not return URL"));
                            }
                            
                            // Plan 업데이트
                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                String jsonContent = objectMapper.writeValueAsString(req);
                                plan.setPlanContent(jsonContent);
                                plan.setTitle(req.getTitle()); 
                                plan.setUrl(designUrl);
                                return planRepository.save(plan);
                            } catch (Exception e) {
                                return Mono.error(new RuntimeException("Failed to serialize plan content", e));
                            }
                        });
                });
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), savedPlan.getUrl()));
    }

    public Mono<DesignCreateResponse> createMockType1(DesignCreateBrandRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                // 모든 섹션을 병렬로 생성
                return Mono.zip(
                    createSection("title", req.getTitle(), req.getTitleImages()),
                    createSection("main_banner", req.getMainBanner(), req.getMainBannerImages()),
                    createSection("coupon_section", req.getCouponSection(), req.getCouponSectionImages()),
                    createSection("product_section", req.getProductSection(), req.getProductSectionImages()),
                    createSection("event_notes", req.getEventNotes(), req.getEventNotesImages())
                )
                .flatMap(tuple -> {
                    // 새로운 API 형식으로 데이터 변환
                    Map<String, Object> apiPayload = new HashMap<>();
                    List<Map<String, Object>> sections = new ArrayList<>();
                    sections.add(tuple.getT1());
                    sections.add(tuple.getT2());
                    sections.add(tuple.getT3());
                    sections.add(tuple.getT4());
                    sections.add(tuple.getT5());
                    
                    apiPayload.put("sections", sections);
                    
                    // 변환된 결과 로깅 (API 호출 직전)
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String requestJson = objectMapper.writeValueAsString(apiPayload);
                        logger.info("=== Mock Creazy API Request (createMockType1) ===");
                        logger.info("URL: https://creazy.app/api/external/generate-sections");
                        logger.info("Request Body: {}", requestJson);
                    } catch (Exception e) {
                        logger.error("Failed to log request: {}", e.getMessage());
                    }
                    
                    // Plan 업데이트 (Mock URL 사용)
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String jsonContent = objectMapper.writeValueAsString(req);
                        plan.setPlanContent(jsonContent);
                        plan.setTitle(req.getTitle());
                        plan.setUrl("https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6");
                        return planRepository.save(plan);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to serialize plan content", e));
                    }
                });
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }

    public Mono<DesignCreateResponse> createMockType2(DesignCreateCategoryRequest req) {
        return planRepository.findByPlanId(req.getPlanId())
            .switchIfEmpty(Mono.error(new RuntimeException("Plan not found")))
            .flatMap(plan -> {
                // 모든 섹션을 병렬로 생성
                return Mono.zip(
                    createSection("title", req.getTitle(), req.getTitleImages()),
                    createSection("main_banner", req.getMainBanner(), req.getMainBannerImages()),
                    createSection("section1", req.getSection1(), req.getSection1Images()),
                    createSection("section2", req.getSection2(), req.getSection2Images()),
                    createSection("section3", req.getSection3(), req.getSection3Images())
                )
                .flatMap(tuple -> {
                    // 새로운 API 형식으로 데이터 변환
                    Map<String, Object> apiPayload = new HashMap<>();
                    List<Map<String, Object>> sections = new ArrayList<>();
                    sections.add(tuple.getT1());
                    sections.add(tuple.getT2());
                    sections.add(tuple.getT3());
                    sections.add(tuple.getT4());
                    sections.add(tuple.getT5());
                    
                    apiPayload.put("sections", sections);
                    
                    // 변환된 결과 로깅 (API 호출 직전)
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        String requestJson = objectMapper.writeValueAsString(apiPayload);
                        logger.info("=== Mock Creazy API Request (createMockType2) ===");
                        logger.info("URL: https://creazy.app/api/external/generate-sections");
                        logger.info("Request Body: {}", requestJson);
                    } catch (Exception e) {
                        logger.error("Failed to log request: {}", e.getMessage());
                    }
                    
                    // Plan 업데이트 (Mock URL 사용)
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        String jsonContent = objectMapper.writeValueAsString(req);
                        plan.setPlanContent(jsonContent);
                        plan.setTitle(req.getTitle());
                        plan.setUrl("https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6");
                        return planRepository.save(plan);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to serialize plan content", e));
                    }
                });
            })
            .map(savedPlan -> new DesignCreateResponse(req.getPlanId(), "https://drive.google.com/uc?export=download&id=1j7ttxTtW5FCcKSwSQVCqwbp4_dFIGYt6"));
    }

}
