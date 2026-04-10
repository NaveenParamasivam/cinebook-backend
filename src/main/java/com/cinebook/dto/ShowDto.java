package com.cinebook.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class ShowDto {

    @Data
    public static class CreateShowRequest {
        @NotNull
        private Long movieId;
        @NotNull
        private Long theaterId;
        @NotNull
        private LocalDate showDate;
        @NotNull
        private LocalTime showTime;
        @NotNull
        private BigDecimal ticketPrice;
    }

    @Data
    public static class ShowResponse {
        private Long id;
        private MovieDto.MovieResponse movie;
        private TheaterDto.TheaterResponse theater;
        private LocalDate showDate;
        private LocalTime showTime;
        private BigDecimal ticketPrice;
        private boolean active;
        private long availableSeats;
        private long totalSeats;
        private LocalDateTime createdAt;
    }
}
