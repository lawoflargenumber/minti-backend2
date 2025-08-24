package com.example.gateway.application.upload;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.example.gateway.domain.upload.Upload;
import com.example.gateway.infra.mongo.UploadRepository;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import com.azure.storage.blob.models.BlobHttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class UploadService {

    private final UploadRepository uploadRepository;
    private final BlobContainerClient blobContainerClient;

    public UploadService(UploadRepository uploadRepository,
                         BlobContainerClient blobContainerClient) {
        this.uploadRepository = uploadRepository;
        this.blobContainerClient = blobContainerClient;
    }

    public Mono<String> save(String planId, FilePart filePart) {
        final String imageId = UUID.randomUUID().toString();
        final String filename = imageId + "-" + filePart.filename();

        final String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return DataBufferUtils.join(filePart.content())
            .map(this::toBytesAndRelease)
            .flatMap(bytes ->
                uploadToBlobStorage(filename, bytes, contentType)
                    .flatMap(sasUrl -> {
                        Upload upload = toUpload(planId, imageId, filePart.filename(), sasUrl);
                        return uploadRepository.save(upload).thenReturn(sasUrl);
                    })
            );
    }

    private byte[] toBytesAndRelease(DataBuffer dataBuffer) {
        try {
            byte[] bytes = new byte[dataBuffer.readableByteCount()];
            dataBuffer.read(bytes);
            return bytes;
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private Upload toUpload(String planId, String imageId, String originalFilename, String url) {
        Upload u = new Upload();
        u.setPlanId(planId);
        u.setImageId(imageId);
        u.setFilename(originalFilename);
        u.setPath(url); 
        u.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return u;
    }

    private Mono<String> uploadToBlobStorage(String filename, byte[] fileBytes, String contentType) {
        return Mono.fromCallable(() -> {
            BlobClient blobClient = blobContainerClient.getBlobClient(filename);

            // 업로드(덮어쓰기 허용)
            blobClient.upload(BinaryData.fromBytes(fileBytes), true);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);
            blobClient.setHttpHeaders(headers);

            BlobSasPermission perms = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(
                    now.plusHours(1), // 만료 시간
                    perms
            ).setStartTime(now.minusMinutes(1)); // 클럭 스큐 완화

            String sasToken = blobClient.generateSas(sasValues);
            return blobClient.getBlobUrl() + "?" + sasToken;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
