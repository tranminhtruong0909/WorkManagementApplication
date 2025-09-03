package com.example.workmanager.dto.response;

import com.example.workmanager.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private String name;
    private String email;
    private String avatarUrl;

    public UserResponse(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
        this.avatarUrl = user.getAvatarUrl();
    }

}