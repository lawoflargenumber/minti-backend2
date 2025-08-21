package com.example.gateway.application.upload;

import com.example.gateway.domain.upload.Upload;
import com.example.gateway.infra.mongo.UploadRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;

    @Value("${app.uploads.dir}")
    private String uploadDir;

    public UploadService(UploadRepository uploadRepository) {
        this.uploadRepository = uploadRepository;
    }

    public Mono<String> save(String planId, FilePart filePart) {
        String imageId = UUID.randomUUID().toString();
        Path dest = Path.of(uploadDir, imageId + "-" + filePart.filename());
        return filePart.transferTo(dest).then(
            uploadRepository.save(toUpload(planId, imageId, filePart.filename(), dest.toString()))
                .map(u -> imageId)
        );
    }

    private Upload toUpload(String planId, String imageId, String filename, String path) {
        Upload u = new Upload();
        u.setPlan_id(planId);
        u.setImageId(imageId);
        u.setFilename(filename);
        u.setPath(path);
        u.setCreatedAt(OffsetDateTime.now());
        return u;
    }
}