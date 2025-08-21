package com.example.gateway.domain.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Table("users")
public class User {
    @Id
    private Long id; // R2DBC requires simple id; we can also use UUID via converters
    private String externalId; // Azure sub (future)
    private String name;
    private String email;
    private String company;
    private OffsetDateTime createdAt;

    public User() {}

    public User(Long id, String externalId, String name, String email, String company, OffsetDateTime createdAt) {
        this.id = id;
        this.externalId = externalId;
        this.name = name;
        this.email = email;
        this.company = company;
        this.createdAt = createdAt;
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}