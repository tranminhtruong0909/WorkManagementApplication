package com.example.workmanager.dto;

import com.example.workmanager.model.User;

public class UserResponse {
    private String name;
    private String email;
    private String avatarUrl;

    public UserResponse(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}