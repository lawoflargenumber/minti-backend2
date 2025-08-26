package com.example.gateway.api.design;

import com.example.gateway.application.upload.UploadService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import jakarta.validation.Validator;
import com.example.gateway.application.design.DesignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.databind.JsonNode;
import com.example.gateway.api.dto.DesignDtos.DesignCreateResponse;
import com.example.gateway.api.dto.DesignDtos.DesignCreateBrandRequest;
import com.example.gateway.api.dto.DesignDtos.DesignCreateCategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ValidationException;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/design")
public class DesignUploadController {

    private final UploadService uploadService;

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final DesignService designService;

    public DesignUploadController(UploadService uploadService, ObjectMapper objectMapper, Validator validator, DesignService designService) {
        this.uploadService = uploadService;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.designService = designService;
    }

    enum Kind { BRAND, CATEGORY, UNKNOWN }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, String>> upload(@RequestPart("file") FilePart file, @RequestPart("planId") String planId) {
        return uploadService.save(planId, file)
                .map(id -> Map.of("imageId", id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DesignCreateResponse>> create(@RequestBody Mono<JsonNode> bodyMono) {
        return bodyMono.flatMap(root -> {
            Kind kind = detectKind(root);
            return switch (kind) {
                case BRAND -> {
                    DesignCreateBrandRequest dto = objectMapper.convertValue(root, DesignCreateBrandRequest.class);
                    validate(dto);
                    yield designService.createType1(dto).map(ResponseEntity::ok);
                }
                case CATEGORY -> {
                    DesignCreateCategoryRequest dto = objectMapper.convertValue(root, DesignCreateCategoryRequest.class);
                    validate(dto);
                    yield designService.createType2(dto).map(ResponseEntity::ok);
                }
                case UNKNOWN -> Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "요청 바디 구조로 BRAND/CATEGORY를 판별할 수 없습니다."
                ));
            };
        });
    }

    @PostMapping(value="/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<DesignCreateResponse>> createTest(@RequestBody Mono<JsonNode> bodyMono) {
        return bodyMono.flatMap(root -> {
            Kind kind = detectKind(root);
            return switch (kind) {
                case BRAND -> {
                    DesignCreateBrandRequest dto = objectMapper.convertValue(root, DesignCreateBrandRequest.class);
                    validate(dto);
                    yield designService.createMockType1(dto).map(ResponseEntity::ok);
                }
                case CATEGORY -> {
                    DesignCreateCategoryRequest dto = objectMapper.convertValue(root, DesignCreateCategoryRequest.class);
                    validate(dto);
                    yield designService.createMockType2(dto).map(ResponseEntity::ok);
                }
                case UNKNOWN -> Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "요청 바디 구조로 BRAND/CATEGORY를 판별할 수 없습니다."
                ));
            };
        });
    }

    private Kind detectKind(JsonNode root) {
        if (root == null || !root.isObject()) return Kind.UNKNOWN;

        boolean hasCommon =
                has(root, "title") &&
                hasArray(root, "titleImages") &&
                has(root, "mainBanner") &&
                hasArray(root, "mainBannerImages");

        boolean looksType1 =
                hasCommon &&
                has(root, "couponSection") && hasArray(root, "couponSectionImages") &&
                has(root, "productSection") && hasArray(root, "productSectionImages") &&
                has(root, "eventNotes") && hasArray(root, "eventNotesImages");

        boolean looksType2 =
                hasCommon &&
                has(root, "section1") && hasArray(root, "section1Images") &&
                has(root, "section2") && hasArray(root, "section2Images") &&
                has(root, "section3") && hasArray(root, "section3Images");

        if (looksType1 ^ looksType2) return looksType1 ? Kind.BRAND : Kind.CATEGORY;

        return Kind.UNKNOWN;
    }

    private boolean has(JsonNode root, String field) {
        return root.hasNonNull(field) && !root.get(field).isNull();
    }

    private boolean hasArray(JsonNode root, String field) {
        return root.has(field) && root.get(field).isArray();
    }

    private <T> void validate(T dto) {
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + " " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효성 검사 실패: " + message);
        }
    }


}