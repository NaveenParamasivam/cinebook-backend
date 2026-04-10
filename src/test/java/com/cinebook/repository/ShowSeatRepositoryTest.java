package com.cinebook.repository;

import com.cinebook.entity.Movie;
import com.cinebook.entity.Show;
import com.cinebook.entity.ShowSeat;
import com.cinebook.entity.Theater;
import com.cinebook.enums.Genre;
import com.cinebook.enums.SeatStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ShowSeatRepositoryTest {

    @Autowired
    ShowSeatRepository showSeatRepository;

    @Autowired
    ShowRepository showRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    TheaterRepository theaterRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    @DisplayName("releaseExpiredLocks sets LOCKED seats back to AVAILABLE when expired")
    void releaseExpiredLocks_updatesExpiredRows() {
        Movie movie = movieRepository.save(Movie.builder().title("M").genre(Genre.ACTION).active(true).build());
        Theater theater = theaterRepository.save(Theater.builder()
                .name("T").city("C").totalRows(1).seatsPerRow(2).totalSeats(2).active(true)
                .build());
        Show show = showRepository.save(Show.builder()
                .movie(movie)
                .theater(theater)
                .showDate(LocalDate.now())
                .showTime(LocalTime.NOON)
                .ticketPrice(new BigDecimal("100.00"))
                .active(true)
                .build());

        ShowSeat expired = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(1)
                .status(SeatStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().minusMinutes(1))
                .build());
        ShowSeat notExpired = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(2)
                .status(SeatStatus.LOCKED)
                .lockedUntil(LocalDateTime.now().plusMinutes(5))
                .build());

        int updated = showSeatRepository.releaseExpiredLocks(LocalDateTime.now());

        assertThat(updated).isEqualTo(1);
        entityManager.clear(); // bulk update bypasses persistence context
        ShowSeat expiredAfter = showSeatRepository.findById(expired.getId()).orElseThrow();
        ShowSeat notExpiredAfter = showSeatRepository.findById(notExpired.getId()).orElseThrow();
        assertThat(expiredAfter.getStatus()).isEqualTo(SeatStatus.AVAILABLE);
        assertThat(expiredAfter.getLockedUntil()).isNull();
        assertThat(notExpiredAfter.getStatus()).isEqualTo(SeatStatus.LOCKED);
    }
}

