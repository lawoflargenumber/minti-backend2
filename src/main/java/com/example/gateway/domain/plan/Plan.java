package com.example.gateway.domain.plan;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.OffsetDateTime;

@Document(collection = "plans")
public class Plan {
    @Id
    private String _id; 
    
    @Field("plan_id")
    private String planId; 
    
    @Field("created_at")
    private OffsetDateTime createdAt;
    
    @Field("last_updated_at")
    private OffsetDateTime lastUpdatedAt;
    
    @Field("target_type")
    private String targetType;
    
    @Field("plan_content")
    private String planContent; 
    
    @Field("url")
    private String url;
    
    @Field("share")
    private Boolean share;
    
    @Field("company")
    private String company;
    
    @Field("title")
    private String title;
    
    @Field("user_id")
    private String userId;

    public Plan() {}

    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    public OffsetDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(OffsetDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public String getPlanContent() { return planContent; }
    public void setPlanContent(String planContent) { this.planContent = planContent; }
    
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    
    public Boolean getShare() { return share; }
    public void setShare(Boolean share) { this.share = share; }
    
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}