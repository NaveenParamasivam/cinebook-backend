package com.cinebook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class TheaterDto {

    @Data
    public static class CreateTheaterRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String city;
        private String address;
        private String state;
        private String pincode;
        private String phone;
        @NotNull
        private Integer totalRows;
        @NotNull
        private Integer seatsPerRow;
    }

    @Data
    public static class UpdateTheaterRequest {
        private String name;
        private String city;
        private String address;
        private String state;
        private String pincode;
        private String phone;
        private Boolean active;
    }

    @Data
    public static class TheaterResponse {
        private Long id;
        private String name;
        private String city;
        private String address;
        private String state;
        private String pincode;
        private String phone;
        private Integer totalSeats;
        private Integer totalRows;
        private Integer seatsPerRow;
        private boolean active;
        private LocalDateTime createdAt;
    }
}
