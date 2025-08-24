package com.example.gateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.time.OffsetDateTime;

public class PlanDtos {

    public static class CreatePlanFromChatRequest {
        @NotBlank
        public String chatId;
    }

    public static class BrandPlanResponse {
        public String planId;
        public String title;
        public String mainBanner;
        public String couponSection;
        public String productSection;
        public String eventNotes;
    }

    public static class CategoryPlanResponse {
        public String planId;
        public String title;
        public String mainBanner;
        public String section1;
        public String section2;
        public String section3;
    }

    public static class NewPlanResponse {
        public String planId;
        public String type;
    }

    public static class DesignRequestBrand {
        @NotBlank public String planId;
        @NotBlank public String title;
        public List<String> titleImages;
        public String mainBanner;
        public List<String> mainBannerImages;
        public String couponSection;
        public List<String> couponSectionImages;
        public String productSection;
        public List<String> productSectionImages;
        public String eventNotes;
        public List<String> eventNotesImages;
    }

    public static class DesignRequestCategory {
        @NotBlank public String planId;
        @NotBlank public String title;
        public List<String> titleImages;
        public String mainBanner;
        public List<String> mainBannerImages;
        public String section1;
        public List<String> section1Images;
        public String section2;
        public List<String> section2Images;
        public String section3;
        public List<String> section3Images;
    }

    public static class DesignResponse {
        public String planId;
        public String designUrl;
    }

    public static class ShareDesignRequest {
        @NotBlank public String planId;
    }

    public static class GetDesignRequest {
        public String planId;
    }

    public static class GetDesignResponse {
        public String title;
        public String targetType; 
        public OffsetDateTime createdAt; 
        public String url;
    }
}