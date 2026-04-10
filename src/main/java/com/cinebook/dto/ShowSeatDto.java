package com.cinebook.dto;

import com.cinebook.enums.SeatStatus;
import lombok.Data;

import java.util.List;

public class ShowSeatDto {

    @Data
    public static class SeatResponse {
        private Long id;
        private String rowLabel;
        private Integer seatNumber;
        private String seatLabel;
        private SeatStatus status;
    }

    @Data
    public static class LockSeatsRequest {
        private List<Long> seatIds;
    }

    @Data
    public static class LockSeatsResponse {
        private List<SeatResponse> lockedSeats;
        private String lockExpiresAt;
        private String message;
    }
}
