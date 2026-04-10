package com.cinebook.controller;

import com.cinebook.dto.ApiResponse;
import com.cinebook.dto.ShowSeatDto;
import com.cinebook.service.impl.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @GetMapping("/show/{showId}")
    public ResponseEntity<ApiResponse<List<ShowSeatDto.SeatResponse>>> getSeats(@PathVariable Long showId) {
        return ResponseEntity.ok(ApiResponse.success(seatService.getSeatsByShow(showId)));
    }

    @PostMapping("/show/{showId}/lock")
    public ResponseEntity<ApiResponse<ShowSeatDto.LockSeatsResponse>> lockSeats(
            @PathVariable Long showId,
            @RequestBody ShowSeatDto.LockSeatsRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                seatService.lockSeats(showId, request.getSeatIds(), userDetails.getUsername())));
    }
}
