package com.example.gateway.api.design;

import com.example.gateway.application.upload.UploadService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/design")
public class DesignUploadController {

    private final UploadService uploadService;

    public DesignUploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value="/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Map<String, String>> upload(@RequestPart("file") FilePart file, @RequestPart("planId") String planId) {
        return uploadService.save(planId, file)
                .map(id -> Map.of("imageId", id));
    }
}