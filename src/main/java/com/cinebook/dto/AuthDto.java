package com.cinebook.dto;

import com.cinebook.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ============================================================
// Auth DTOs
// ============================================================
public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String phone;
    }

    @Data
    public static class AdminRegisterRequest {
        @NotBlank @Email
        private String email;
        @NotBlank @Size(min = 6, max = 100)
        private String password;
        @NotBlank
        private String firstName;
        @NotBlank
        private String lastName;
        private String phone;
        /** Must match ADMIN_REGISTRATION_SECRET in .env to create an admin account */
        @NotBlank
        private String adminSecret;
    }

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private UserDto.UserResponse user;

        public AuthResponse(String accessToken, UserDto.UserResponse user) {
            this.accessToken = accessToken;
            this.user = user;
        }
    }
}
