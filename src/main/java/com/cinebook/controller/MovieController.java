package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.MovieDto;
import com.cinebook.enums.Genre;
import com.cinebook.service.impl.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDto.MovieResponse>>> getAllMovies() {
        return ResponseEntity.ok(ApiResponse.success(movieService.getAllActiveMovies()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDto.MovieResponse>> getMovie(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getMovieById(id)));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<List<MovieDto.MovieResponse>>> getByGenre(@PathVariable Genre genre) {
        return ResponseEntity.ok(ApiResponse.success(movieService.getMoviesByGenre(genre)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MovieDto.MovieResponse>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(movieService.searchMovies(q)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDto.MovieResponse>> createMovie(
            @Valid @RequestBody MovieDto.CreateMovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie created", movieService.createMovie(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MovieDto.MovieResponse>> updateMovie(
            @PathVariable Long id,
            @RequestBody MovieDto.UpdateMovieRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Movie updated", movieService.updateMovie(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie deleted", null));
    }
}
