package com.cinebook.dto;

import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDto {

    @Data
    public static class CreateBookingRequest {
        @NotNull
        private Long showId;
        @NotNull
        private List<Long> seatIds;
    }

    @Data
    public static class BookingResponse {
        private Long id;
        private String bookingReference;
        private UserDto.UserResponse user;
        private ShowDto.ShowResponse show;
        private List<ShowSeatDto.SeatResponse> seats;
        private Integer seatCount;
        private BigDecimal totalAmount;
        private BookingStatus bookingStatus;
        private PaymentStatus paymentStatus;
        private String razorpayOrderId;
        private LocalDateTime createdAt;
    }

    @Data
    public static class PaymentVerificationRequest {
        @NotNull
        private String razorpayOrderId;
        @NotNull
        private String razorpayPaymentId;
        @NotNull
        private String razorpaySignature;
        @NotNull
        private Long bookingId;
    }

    @Data
    public static class RazorpayOrderResponse {
        private String orderId;
        private BigDecimal amount;
        private String currency;
        private String bookingReference;
        private Long bookingId;
        private String keyId;
    }
}
