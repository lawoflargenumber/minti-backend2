package com.example.gateway.domain.plan;

public enum TargetType {
    BRAND("brand"),
    CATEGORY("category");
    
    private final String value;
    
    TargetType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static TargetType fromValue(String value) {
        for (TargetType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown target type: " + value);
    }
}
