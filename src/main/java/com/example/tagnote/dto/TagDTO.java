package com.example.tagnote.dto;

import java.time.LocalDateTime;

public class TagDTO {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private String username;

    public TagDTO() {
    }

    public TagDTO(Long id, String name, LocalDateTime createdAt, String username) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.username = username;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}