package com.example.gateway.domain.upload;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;

@Document(collection = "uploads")
public class Upload {

    @Id
    private String _id;

    private String imageId;

    private String url;
    
    @Field("plan_id")
    private String planId; 

    private String filename;

    private String path;

    @Field("created_at")
    private OffsetDateTime createdAt;

    public Upload() {}

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getImageId() { return imageId; }
    public void setImageId(String imageId) { this.imageId = imageId; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}