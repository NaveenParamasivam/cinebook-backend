package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.TheaterDto;
import com.cinebook.service.impl.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TheaterDto.TheaterResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(theaterService.getAllActiveTheaters()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TheaterDto.TheaterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(theaterService.getTheaterById(id)));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<TheaterDto.TheaterResponse>>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(ApiResponse.success(theaterService.getTheatersByCity(city)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TheaterDto.TheaterResponse>> create(
            @Valid @RequestBody TheaterDto.CreateTheaterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Theater created", theaterService.createTheater(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TheaterDto.TheaterResponse>> update(
            @PathVariable Long id,
            @RequestBody TheaterDto.UpdateTheaterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Theater updated", theaterService.updateTheater(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.ok(ApiResponse.success("Theater deleted", null));
    }
}
