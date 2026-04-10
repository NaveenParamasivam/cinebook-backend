package com.cinebook.dto;

import com.cinebook.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    public static class UserResponse {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private Role role;
        private LocalDateTime createdAt;
    }

    @Data
    public static class UpdateProfileRequest {
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String phone;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank
        private String currentPassword;
        @NotBlank
        private String newPassword;
    }
}
