package com.example.workmanager.dto;

import com.example.workmanager.model.User;

public class UserResponse {
    private Integer id;
    private String username;
    private String email;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail(); // nếu có
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }
}
