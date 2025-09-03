package com.example.workmanager.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @NotNull(message = "Email không được null")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @NotNull(message = "Mật khẩu không được null")
    @Size(min = 1, message = "Mật khẩu phải có ít nhất 1 ký tự")
    private String password;
}