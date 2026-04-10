package com.cinebook.dto;

import com.cinebook.enums.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MovieDto {

    @Data
    public static class CreateMovieRequest {
        @NotBlank
        private String title;
        private String description;
        @NotNull
        private Genre genre;
        private Integer durationMinutes;
        private String rating;
        private Double imdbRating;
        private String language;
        private String posterUrl;
        private String trailerUrl;
        private LocalDate releaseDate;
        private String director;
        private String cast;
    }

    @Data
    public static class UpdateMovieRequest {
        private String title;
        private String description;
        private Genre genre;
        private Integer durationMinutes;
        private String rating;
        private Double imdbRating;
        private String language;
        private String posterUrl;
        private String trailerUrl;
        private LocalDate releaseDate;
        private String director;
        private String cast;
        private Boolean active;
    }

    @Data
    public static class MovieResponse {
        private Long id;
        private String title;
        private String description;
        private Genre genre;
        private Integer durationMinutes;
        private String rating;
        private Double imdbRating;
        private String language;
        private String posterUrl;
        private String trailerUrl;
        private LocalDate releaseDate;
        private String director;
        private String cast;
        private boolean active;
        private LocalDateTime createdAt;
    }
}
