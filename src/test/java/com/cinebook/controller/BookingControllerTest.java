package com.cinebook.controller;

import com.cinebook.dto.BookingDto;
import com.cinebook.exception.GlobalExceptionHandler;
import com.cinebook.security.JwtAuthenticationFilter;
import com.cinebook.service.impl.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import({GlobalExceptionHandler.class, com.cinebook.config.SecurityConfig.class})
class BookingControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BookingService bookingService;

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
    @DisplayName("POST /bookings/initiate requires authentication")
    void initiateBooking_requiresAuth() throws Exception {
        BookingDto.CreateBookingRequest req = new BookingDto.CreateBookingRequest();
        req.setShowId(1L);
        req.setSeatIds(List.of(1L, 2L));

        mockMvc.perform(post("/bookings/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /bookings/initiate validates request body and returns 400")
    void initiateBooking_validationError_is400() throws Exception {
        BookingDto.CreateBookingRequest req = new BookingDto.CreateBookingRequest();
        req.setShowId(null);
        req.setSeatIds(null);

        mockMvc.perform(post("/bookings/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.showId").exists())
                .andExpect(jsonPath("$.data.seatIds").exists());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    @DisplayName("POST /bookings/initiate returns 201 with order data")
    void initiateBooking_created() throws Exception {
        BookingDto.CreateBookingRequest req = new BookingDto.CreateBookingRequest();
        req.setShowId(1L);
        req.setSeatIds(List.of(10L, 11L));

        BookingDto.RazorpayOrderResponse resp = new BookingDto.RazorpayOrderResponse();
        resp.setOrderId("order_123");
        resp.setCurrency("INR");
        resp.setAmount(new BigDecimal("500.00"));
        resp.setBookingId(99L);

        when(bookingService.initiateBooking(any(BookingDto.CreateBookingRequest.class), eq("user@example.com")))
                .thenReturn(resp);

        mockMvc.perform(post("/bookings/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created"))
                .andExpect(jsonPath("$.data.orderId").value("order_123"))
                .andExpect(jsonPath("$.data.bookingId").value(99));
    }
}

