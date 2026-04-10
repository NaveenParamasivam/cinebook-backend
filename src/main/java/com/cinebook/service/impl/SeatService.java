package com.cinebook.service.impl;

import com.cinebook.dto.ShowSeatDto;
import com.cinebook.entity.ShowSeat;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.BadRequestException;
import com.cinebook.repository.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private static final int LOCK_DURATION_MINUTES = 10;
    private final ShowSeatRepository showSeatRepository;

    public List<ShowSeatDto.SeatResponse> getSeatsByShow(Long showId) {
        return showSeatRepository.findByShowIdOrderByRowLabelAscSeatNumberAsc(showId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ShowSeatDto.LockSeatsResponse lockSeats(Long showId, List<Long> seatIds, String userEmail) {
        List<ShowSeat> seats = showSeatRepository.findByShowIdAndIdIn(showId, seatIds);

        if (seats.size() != seatIds.size()) {
            throw new BadRequestException("One or more seats not found for this show");
        }

        LocalDateTime now = LocalDateTime.now();
        for (ShowSeat seat : seats) {
            if (seat.getStatus() == SeatStatus.BOOKED) {
                throw new BadRequestException("Seat " + seat.getSeatLabel() + " is already booked");
            }
            if (seat.getStatus() == SeatStatus.LOCKED && seat.getLockedUntil() != null
                    && seat.getLockedUntil().isAfter(now)) {
                throw new BadRequestException("Seat " + seat.getSeatLabel() + " is temporarily held by another user");
            }
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockedUntil(now.plusMinutes(LOCK_DURATION_MINUTES));
        }
        showSeatRepository.saveAll(seats);

        LocalDateTime lockExpiry = now.plusMinutes(LOCK_DURATION_MINUTES);
        ShowSeatDto.LockSeatsResponse response = new ShowSeatDto.LockSeatsResponse();
        response.setLockedSeats(seats.stream().map(this::toResponse).toList());
        response.setLockExpiresAt(lockExpiry.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.setMessage("Seats locked for " + LOCK_DURATION_MINUTES + " minutes. Complete payment to confirm.");
        return response;
    }

    @Transactional
    public void releaseSeats(List<Long> seatIds) {
        List<ShowSeat> seats = showSeatRepository.findAllById(seatIds);
        seats.forEach(seat -> {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedUntil(null);
            seat.setBooking(null);
        });
        showSeatRepository.saveAll(seats);
    }

    // Run every 2 minutes to clean up expired locks
    @Scheduled(fixedDelay = 120000)
    @Transactional
    public void releaseExpiredLocks() {
        int released = showSeatRepository.releaseExpiredLocks(LocalDateTime.now());
        if (released > 0) {
            log.info("[SeatService] Released {} expired seat locks", released);
        }
    }

    public ShowSeatDto.SeatResponse toResponse(ShowSeat s) {
        ShowSeatDto.SeatResponse r = new ShowSeatDto.SeatResponse();
        r.setId(s.getId());
        r.setRowLabel(s.getRowLabel());
        r.setSeatNumber(s.getSeatNumber());
        r.setSeatLabel(s.getSeatLabel());

        // If lock expired, show as AVAILABLE
        if (s.getStatus() == SeatStatus.LOCKED && s.getLockedUntil() != null
                && s.getLockedUntil().isBefore(LocalDateTime.now())) {
            r.setStatus(SeatStatus.AVAILABLE);
        } else {
            r.setStatus(s.getStatus());
        }
        return r;
    }
}
