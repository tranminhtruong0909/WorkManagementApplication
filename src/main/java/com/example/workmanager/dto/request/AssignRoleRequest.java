package com.example.workmanager.dto.request;

import lombok.Data;

@Data
public class AssignRoleRequest {
    private Integer userId;  // Thêm trường userId
    private String role;     // ADMIN, MANAGER, MEMBER, VIEWER
}

