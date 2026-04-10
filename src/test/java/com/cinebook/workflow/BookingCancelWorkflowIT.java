package com.cinebook.workflow;

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
import com.cinebook.repository.BookingRepository;
import com.cinebook.repository.MovieRepository;
import com.cinebook.repository.ShowRepository;
import com.cinebook.repository.ShowSeatRepository;
import com.cinebook.repository.TheaterRepository;
import com.cinebook.repository.UserRepository;
import com.cinebook.service.impl.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingCancelWorkflowIT {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired MovieRepository movieRepository;
    @Autowired TheaterRepository theaterRepository;
    @Autowired ShowRepository showRepository;
    @Autowired ShowSeatRepository showSeatRepository;
    @Autowired BookingRepository bookingRepository;

    @MockBean EmailService emailService; // avoid real email work

    @Test
    @DisplayName("Cancel booking workflow: API -> service -> DB releases seats and marks booking cancelled")
    void cancelBooking_endToEnd() throws Exception {
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
        booking.setSeats(List.of(s1, s2));
        bookingRepository.save(booking);

        mockMvc.perform(post("/bookings/{id}/cancel", booking.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("user@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Booking cancelled"))
                .andExpect(jsonPath("$.data.bookingStatus").value("CANCELLED"));

        Booking after = bookingRepository.findByIdWithDetails(booking.getId()).orElseThrow();
        assertThat(after.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(after.getSeats()).allMatch(ss -> ss.getStatus() == SeatStatus.AVAILABLE);
        assertThat(after.getSeats()).allMatch(ss -> ss.getBooking() == null);
    }
}

