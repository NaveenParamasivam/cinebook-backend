package com.cinebook.workflow;

import com.cinebook.entity.Movie;
import com.cinebook.entity.Show;
import com.cinebook.entity.ShowSeat;
import com.cinebook.entity.Theater;
import com.cinebook.enums.Genre;
import com.cinebook.enums.SeatStatus;
import com.cinebook.repository.MovieRepository;
import com.cinebook.repository.ShowRepository;
import com.cinebook.repository.ShowSeatRepository;
import com.cinebook.repository.TheaterRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class SeatLockWorkflowIT {

    @Autowired MockMvc mockMvc;
    @Autowired MovieRepository movieRepository;
    @Autowired TheaterRepository theaterRepository;
    @Autowired ShowRepository showRepository;
    @Autowired ShowSeatRepository showSeatRepository;

    @Test
    @DisplayName("Seat locking workflow: API -> service -> DB persists LOCKED status")
    void lockSeats_endToEnd() throws Exception {
        Movie movie = movieRepository.save(Movie.builder().title("M").genre(Genre.ACTION).active(true).build());
        Theater theater = theaterRepository.save(Theater.builder()
                .name("T").city("C").totalRows(1).seatsPerRow(2).totalSeats(2).active(true).build());
        Show show = showRepository.save(Show.builder()
                .movie(movie).theater(theater)
                .showDate(LocalDate.now()).showTime(LocalTime.NOON)
                .ticketPrice(new BigDecimal("100.00"))
                .active(true)
                .build());

        ShowSeat s1 = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(1).status(SeatStatus.AVAILABLE).build());
        ShowSeat s2 = showSeatRepository.save(ShowSeat.builder()
                .show(show).rowLabel("A").seatNumber(2).status(SeatStatus.AVAILABLE).build());

        String body = """
                {"seatIds":[%d,%d]}
                """.formatted(s1.getId(), s2.getId());

        mockMvc.perform(post("/seats/show/{showId}/lock", show.getId())
                        .with(SecurityMockMvcRequestPostProcessors.user("user@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lockedSeats.length()").value(2))
                .andExpect(jsonPath("$.data.lockExpiresAt").exists());

        List<ShowSeat> persisted = showSeatRepository.findAllById(List.of(s1.getId(), s2.getId()));
        assertThat(persisted).allMatch(ss -> ss.getStatus() == SeatStatus.LOCKED);
        assertThat(persisted).allMatch(ss -> ss.getLockedUntil() != null);
    }
}

