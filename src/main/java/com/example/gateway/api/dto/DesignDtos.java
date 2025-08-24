package com.example.gateway.api.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public final class DesignDtos {

    private DesignDtos() {}

    public record DesignCreateResponse(
            String planId,
            String designUrl
    ) {}

    public static class DesignCreateBrandRequest {

        @NotBlank private String planId;

        @NotBlank private String title;
        @NotNull  private List<@NotBlank String> titleImages;

        @NotBlank private String mainBanner;
        @NotNull  private List<@NotBlank String> mainBannerImages;

        @NotBlank private String couponSection;
        @NotNull  private List<@NotBlank String> couponSectionImages;

        @NotBlank private String productSection;
        @NotNull  private List<@NotBlank String> productSectionImages;

        @NotBlank private String eventNotes;
        @NotNull  private List<@NotBlank String> eventNotesImages;

        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public List<String> getTitleImages() { return titleImages; }
        public void setTitleImages(List<String> titleImages) { this.titleImages = titleImages; }

        public String getMainBanner() { return mainBanner; }
        public void setMainBanner(String mainBanner) { this.mainBanner = mainBanner; }

        public List<String> getMainBannerImages() { return mainBannerImages; }
        public void setMainBannerImages(List<String> mainBannerImages) { this.mainBannerImages = mainBannerImages; }

        public String getCouponSection() { return couponSection; }
        public void setCouponSection(String couponSection) { this.couponSection = couponSection; }

        public List<String> getCouponSectionImages() { return couponSectionImages; }
        public void setCouponSectionImages(List<String> couponSectionImages) { this.couponSectionImages = couponSectionImages; }

        public String getProductSection() { return productSection; }
        public void setProductSection(String productSection) { this.productSection = productSection; }

        public List<String> getProductSectionImages() { return productSectionImages; }
        public void setProductSectionImages(List<String> productSectionImages) { this.productSectionImages = productSectionImages; }

        public String getEventNotes() { return eventNotes; }
        public void setEventNotes(String eventNotes) { this.eventNotes = eventNotes; }

        public List<String> getEventNotesImages() { return eventNotesImages; }
        public void setEventNotesImages(List<String> eventNotesImages) { this.eventNotesImages = eventNotesImages; }
    }

    public static class DesignCreateCategoryRequest {

        @NotBlank private String planId;

        @NotBlank private String title;
        @NotNull  private List<@NotBlank String> titleImages;

        @NotBlank private String mainBanner;
        @NotNull  private List<@NotBlank String> mainBannerImages;

        @NotBlank private String section1;
        @NotNull  private List<@NotBlank String> section1Images;

        @NotBlank private String section2;
        @NotNull  private List<@NotBlank String> section2Images;

        @NotBlank private String section3;
        @NotNull  private List<@NotBlank String> section3Images;

        public String getPlanId() { return planId; }
        public void setPlanId(String planId) { this.planId = planId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public List<String> getTitleImages() { return titleImages; }
        public void setTitleImages(List<String> titleImages) { this.titleImages = titleImages; }

        public String getMainBanner() { return mainBanner; }
        public void setMainBanner(String mainBanner) { this.mainBanner = mainBanner; }

        public List<String> getMainBannerImages() { return mainBannerImages; }
        public void setMainBannerImages(List<String> mainBannerImages) { this.mainBannerImages = mainBannerImages; }

        public String getSection1() { return section1; }
        public void setSection1(String section1) { this.section1 = section1; }

        public List<String> getSection1Images() { return section1Images; }
        public void setSection1Images(List<String> section1Images) { this.section1Images = section1Images; }

        public String getSection2() { return section2; }
        public void setSection2(String section2) { this.section2 = section2; }

        public List<String> getSection2Images() { return section2Images; }
        public void setSection2Images(List<String> section2Images) { this.section2Images = section2Images; }

        public String getSection3() { return section3; }
        public void setSection3(String section3) { this.section3 = section3; }

        public List<String> getSection3Images() { return section3Images; }
        public void setSection3Images(List<String> section3Images) { this.section3Images = section3Images; }
    }
}
