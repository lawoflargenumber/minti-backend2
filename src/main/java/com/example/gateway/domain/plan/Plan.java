package com.example.gateway.domain.plan;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.OffsetDateTime;

@Document(collection = "plans")
public class Plan {
    @Id
    private String _id; // internal
    private String plan_id; // external key
    private String userId;
    private String targetType; // "brand" | "category"

    // common
    private String title;
    private String mainBanner;

    // brand
    private String couponSection;
    private String productSection;
    private String eventNotes;

    // category
    private String section1;
    private String section2;
    private String section3;

    // design
    private String designUrl;

    private OffsetDateTime createdAt;

    public Plan() {}

    // getters/setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public String getPlan_id() { return plan_id; }
    public void setPlan_id(String plan_id) { this.plan_id = plan_id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMainBanner() { return mainBanner; }
    public void setMainBanner(String mainBanner) { this.mainBanner = mainBanner; }
    public String getCouponSection() { return couponSection; }
    public void setCouponSection(String couponSection) { this.couponSection = couponSection; }
    public String getProductSection() { return productSection; }
    public void setProductSection(String productSection) { this.productSection = productSection; }
    public String getEventNotes() { return eventNotes; }
    public void setEventNotes(String eventNotes) { this.eventNotes = eventNotes; }
    public String getSection1() { return section1; }
    public void setSection1(String section1) { this.section1 = section1; }
    public String getSection2() { return section2; }
    public void setSection2(String section2) { this.section2 = section2; }
    public String getSection3() { return section3; }
    public void setSection3(String section3) { this.section3 = section3; }
    public String getDesignUrl() { return designUrl; }
    public void setDesignUrl(String designUrl) { this.designUrl = designUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}