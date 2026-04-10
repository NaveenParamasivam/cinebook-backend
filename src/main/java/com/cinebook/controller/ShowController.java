package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.ShowDto;
import com.cinebook.service.impl.ShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<ApiResponse<List<ShowDto.ShowResponse>>> getByMovieAndDate(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.success(showService.getShowsByMovieAndDate(movieId, date)));
    }

    @GetMapping("/movie/{movieId}/upcoming")
    public ResponseEntity<ApiResponse<List<ShowDto.ShowResponse>>> getUpcoming(@PathVariable Long movieId) {
        return ResponseEntity.ok(ApiResponse.success(showService.getUpcomingShowsByMovie(movieId)));
    }

    @GetMapping("/movie/{movieId}/dates")
    public ResponseEntity<ApiResponse<List<LocalDate>>> getAvailableDates(@PathVariable Long movieId) {
        return ResponseEntity.ok(ApiResponse.success(showService.getAvailableDatesByMovie(movieId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowDto.ShowResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(showService.getShowById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShowDto.ShowResponse>> create(
            @Valid @RequestBody ShowDto.CreateShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show created", showService.createShow(request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        showService.deleteShow(id);
        return ResponseEntity.ok(ApiResponse.success("Show deleted", null));
    }
}
