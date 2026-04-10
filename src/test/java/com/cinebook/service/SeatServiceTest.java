package com.cinebook.service;

import com.cinebook.dto.ShowSeatDto;
import com.cinebook.entity.ShowSeat;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.BadRequestException;
import com.cinebook.repository.ShowSeatRepository;
import com.cinebook.service.impl.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatService Tests")
class SeatServiceTest {

    @Mock
    private ShowSeatRepository showSeatRepository;

    @InjectMocks
    private SeatService seatService;

    private ShowSeat availableSeat;
    private ShowSeat bookedSeat;
    private ShowSeat activeLockSeat;

    @BeforeEach
    void setUp() {
        availableSeat = ShowSeat.builder()
                .id(1L).rowLabel("A").seatNumber(1)
                .status(SeatStatus.AVAILABLE).build();

        bookedSeat = ShowSeat.builder()
                .id(2L).rowLabel("A").seatNumber(2)
                .status(SeatStatus.BOOKED).build();

        activeLockSeat = ShowSeat.builder()
                .id(3L).rowLabel("A").seatNumber(3)
                .status(SeatStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().plusMinutes(5))
                .build();
    }

    @Nested
    @DisplayName("Lock Seats")
    class LockSeatsTests {

        @Test
        @DisplayName("Should lock available seats successfully")
        void shouldLockAvailableSeats() {
            when(showSeatRepository.findByShowIdAndIdIn(1L, List.of(1L)))
                    .thenReturn(List.of(availableSeat));
            when(showSeatRepository.saveAll(any())).thenReturn(List.of(availableSeat));

            ShowSeatDto.LockSeatsResponse response =
                    seatService.lockSeats(1L, List.of(1L), "user@example.com");

            assertThat(response.getLockedSeats()).hasSize(1);
            assertThat(availableSeat.getStatus()).isEqualTo(SeatStatus.LOCKED);
            assertThat(availableSeat.getLockedUntil()).isAfter(LocalDateTime.now());
            verify(showSeatRepository).saveAll(any());
        }

        @Test
        @DisplayName("Should throw BadRequestException when seat is already BOOKED")
        void shouldThrowWhenSeatAlreadyBooked() {
            when(showSeatRepository.findByShowIdAndIdIn(1L, List.of(2L)))
                    .thenReturn(List.of(bookedSeat));

            assertThatThrownBy(() -> seatService.lockSeats(1L, List.of(2L), "user@example.com"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already booked");
        }

        @Test
        @DisplayName("Should throw BadRequestException when seat is actively LOCKED by another user")
        void shouldThrowWhenSeatLockedByAnotherUser() {
            when(showSeatRepository.findByShowIdAndIdIn(1L, List.of(3L)))
                    .thenReturn(List.of(activeLockSeat));

            assertThatThrownBy(() -> seatService.lockSeats(1L, List.of(3L), "user@example.com"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("temporarily held");
        }

        @Test
        @DisplayName("Should throw when seat count mismatch")
        void shouldThrowWhenSeatNotFoundForShow() {
            // Only 1 seat found but 2 requested
            when(showSeatRepository.findByShowIdAndIdIn(1L, List.of(1L, 99L)))
                    .thenReturn(List.of(availableSeat));

            assertThatThrownBy(() -> seatService.lockSeats(1L, List.of(1L, 99L), "user@example.com"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should re-lock seat whose lock has already expired")
        void shouldRelockExpiredSeat() {
            ShowSeat expiredLockSeat = ShowSeat.builder()
                    .id(4L).rowLabel("B").seatNumber(1)
                    .status(SeatStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().minusMinutes(5)) // expired
                    .build();

            when(showSeatRepository.findByShowIdAndIdIn(1L, List.of(4L)))
                    .thenReturn(List.of(expiredLockSeat));
            when(showSeatRepository.saveAll(any())).thenReturn(List.of(expiredLockSeat));

            ShowSeatDto.LockSeatsResponse response =
                    seatService.lockSeats(1L, List.of(4L), "user@example.com");

            assertThat(response.getLockedSeats()).hasSize(1);
            assertThat(expiredLockSeat.getStatus()).isEqualTo(SeatStatus.LOCKED);
            // New expiry should be in the future
            assertThat(expiredLockSeat.getLockedUntil()).isAfter(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("Get Seats By Show")
    class GetSeatsByShowTests {

        @Test
        @DisplayName("Should return all seats for a show sorted by row and number")
        void shouldReturnSeatsSorted() {
            when(showSeatRepository.findByShowIdOrderByRowLabelAscSeatNumberAsc(1L))
                    .thenReturn(List.of(availableSeat, bookedSeat));

            List<ShowSeatDto.SeatResponse> result = seatService.getSeatsByShow(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getSeatLabel()).isEqualTo("A1");
            assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(result.get(1).getStatus()).isEqualTo(SeatStatus.BOOKED);
        }

        @Test
        @DisplayName("Should map expired LOCKED seat as AVAILABLE in response")
        void shouldShowExpiredLockedSeatAsAvailable() {
            ShowSeat expiredLock = ShowSeat.builder()
                    .id(5L).rowLabel("C").seatNumber(1)
                    .status(SeatStatus.LOCKED)
                    .lockedUntil(LocalDateTime.now().minusSeconds(1))
                    .build();

            when(showSeatRepository.findByShowIdOrderByRowLabelAscSeatNumberAsc(1L))
                    .thenReturn(List.of(expiredLock));

            List<ShowSeatDto.SeatResponse> result = seatService.getSeatsByShow(1L);

            // Expired locks should show as AVAILABLE to other users
            assertThat(result.get(0).getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("Release Seats")
    class ReleaseSeatsTests {

        @Test
        @DisplayName("Should release seats back to AVAILABLE")
        void shouldReleaseSeats() {
            availableSeat.setStatus(SeatStatus.LOCKED);
            when(showSeatRepository.findAllById(List.of(1L))).thenReturn(List.of(availableSeat));
            when(showSeatRepository.saveAll(any())).thenReturn(List.of(availableSeat));

            seatService.releaseSeats(List.of(1L));

            assertThat(availableSeat.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
            assertThat(availableSeat.getLockedUntil()).isNull();
            assertThat(availableSeat.getBooking()).isNull();
        }
    }
}
