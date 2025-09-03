package com.example.workmanager.dto.request;

public class UpdateProfileRequest {
    private String name;
    private String email;
    private String avatarUrl;
    private String password; // Mật khẩu hiện tại - BẮT BUỘC để xác thực

    // Constructors
    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String name, String email, String avatarUrl, String password) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.password = password;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}