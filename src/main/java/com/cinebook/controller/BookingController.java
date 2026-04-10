package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.BookingDto;
import com.cinebook.service.impl.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<BookingDto.RazorpayOrderResponse>> initiateBooking(
            @Valid @RequestBody BookingDto.CreateBookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created", bookingService.initiateBooking(request, userDetails.getUsername())));
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<BookingDto.BookingResponse>> verifyPayment(
            @Valid @RequestBody BookingDto.PaymentVerificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Booking confirmed",
                bookingService.verifyPaymentAndConfirm(request, userDetails.getUsername())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BookingDto.BookingResponse>>> myBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getUserBookings(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingDto.BookingResponse>> getBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingById(id, userDetails.getUsername())));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingDto.BookingResponse>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled",
                bookingService.cancelBooking(id, userDetails.getUsername())));
    }
}
