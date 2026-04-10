package com.cinebook.repository;

import com.cinebook.entity.Booking;
import com.cinebook.entity.Movie;
import com.cinebook.entity.Show;
import com.cinebook.entity.ShowSeat;
import com.cinebook.entity.Theater;
import com.cinebook.entity.User;
import com.cinebook.enums.BookingStatus;
import com.cinebook.enums.Genre;
import com.cinebook.enums.PaymentStatus;
import com.cinebook.enums.Role;
import com.cinebook.enums.SeatStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired BookingRepository bookingRepository;
    @Autowired UserRepository userRepository;
    @Autowired ShowRepository showRepository;
    @Autowired ShowSeatRepository showSeatRepository;
    @Autowired MovieRepository movieRepository;
    @Autowired TheaterRepository theaterRepository;

    @Test
    @DisplayName("findByIdWithDetails fetches show, movie, seats without LazyInitialization issues")
    void findByIdWithDetails_fetchGraph() {
        User user = userRepository.save(User.builder()
                .email("user@example.com")
                .password("x")
                .firstName("F")
                .lastName("L")
                .role(Role.ROLE_USER)
                .enabled(true)
                .build());
        Movie movie = movieRepository.save(Movie.builder().title("M").genre(Genre.ACTION).active(true).build());
        Theater theater = theaterRepository.save(Theater.builder()
                .name("T").city("C").totalRows(1).seatsPerRow(2).totalSeats(2).active(true).build());
        Show show = showRepository.save(Show.builder()
                .movie(movie).theater(theater)
                .showDate(LocalDate.now()).showTime(LocalTime.NOON)
                .ticketPrice(new BigDecimal("100.00"))
                .active(true)
                .build());

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .show(show)
                .seatCount(2)
                .totalAmount(new BigDecimal("200.00"))
                .bookingStatus(BookingStatus.CONFIRMED)
                .paymentStatus(PaymentStatus.SUCCESS)
                .build());

        ShowSeat s1 = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(1).status(SeatStatus.BOOKED).booking(booking).build());
        ShowSeat s2 = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(2).status(SeatStatus.BOOKED).booking(booking).build());
        booking.setSeats(new ArrayList<>(List.of(s1, s2)));
        bookingRepository.save(booking);

        Booking loaded = bookingRepository.findByIdWithDetails(booking.getId()).orElseThrow();

        assertThat(loaded.getUser().getEmail()).isEqualTo("user@example.com");
        assertThat(loaded.getShow().getMovie().getTitle()).isEqualTo("M");
        assertThat(loaded.getSeats()).hasSize(2);
    }
}

