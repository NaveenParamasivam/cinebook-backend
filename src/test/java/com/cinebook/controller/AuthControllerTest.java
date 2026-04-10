package com.cinebook.controller;

import com.cinebook.dto.AuthDto;
import com.cinebook.dto.UserDto;
import com.cinebook.enums.Role;
import com.cinebook.exception.GlobalExceptionHandler;
import com.cinebook.security.JwtAuthenticationFilter;
import com.cinebook.service.impl.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, com.cinebook.config.SecurityConfig.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthService authService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, jakarta.servlet.FilterChain.class)
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("POST /auth/register validates input and returns 400")
    void register_validationError_is400() throws Exception {
        AuthDto.RegisterRequest req = new AuthDto.RegisterRequest();
        req.setEmail("not-an-email");
        req.setPassword("123"); // too short
        req.setFirstName("");
        req.setLastName("");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists())
                .andExpect(jsonPath("$.data.firstName").exists())
                .andExpect(jsonPath("$.data.lastName").exists());
    }

    @Test
    @DisplayName("POST /auth/login returns token response")
    void login_ok() throws Exception {
        AuthDto.LoginRequest req = new AuthDto.LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("password123");

        UserDto.UserResponse user = new UserDto.UserResponse();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.ROLE_USER);

        when(authService.login(any(AuthDto.LoginRequest.class)))
                .thenReturn(new AuthDto.AuthResponse("jwt-token", user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("user@example.com"));
    }
}

