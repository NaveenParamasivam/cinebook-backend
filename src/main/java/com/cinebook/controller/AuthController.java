package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.AuthDto;
import com.cinebook.service.impl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> register(
            @Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registered successfully", authService.register(request)));
    }

    /**
     * Register a new admin account.
     * Requires the ADMIN_REGISTRATION_SECRET from your .env file in the request body.
     * This endpoint is public but protected by the secret key.
     */
    @PostMapping("/register-admin")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> registerAdmin(
            @Valid @RequestBody AuthDto.AdminRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin account created successfully", authService.registerAdmin(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
    }
}
