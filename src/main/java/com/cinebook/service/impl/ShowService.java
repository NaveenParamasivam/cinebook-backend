package com.cinebook.service.impl;

import com.cinebook.dto.MovieDto;
import com.cinebook.dto.ShowDto;
import com.cinebook.dto.TheaterDto;
import com.cinebook.entity.Movie;
import com.cinebook.entity.Show;
import com.cinebook.entity.ShowSeat;
import com.cinebook.entity.Theater;
import com.cinebook.enums.SeatStatus;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.MovieRepository;
import com.cinebook.repository.ShowRepository;
import com.cinebook.repository.ShowSeatRepository;
import com.cinebook.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieService movieService;
    private final TheaterService theaterService;

    @Transactional
    public List<ShowDto.ShowResponse> getShowsByMovieAndDate(Long movieId, LocalDate date) {
//        return showRepository.findByMovieIdAndShowDate(movieId, date)
//                .stream().map(this::toResponse).toList();
        return showRepository.findShowsWithMovieAndTheater(movieId, date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<ShowDto.ShowResponse> getUpcomingShowsByMovie(Long movieId) {
        return showRepository.findUpcomingShowsByMovie(movieId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<LocalDate> getAvailableDatesByMovie(Long movieId) {
        return showRepository.findAvailableDatesByMovie(movieId);
    }

    @Transactional
    public ShowDto.ShowResponse getShowById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ShowDto.ShowResponse createShow(ShowDto.CreateShowRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", request.getMovieId()));
        Theater theater = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater", request.getTheaterId()));

        Show show = Show.builder()
                .movie(movie)
                .theater(theater)
                .showDate(request.getShowDate())
                .showTime(request.getShowTime())
                .ticketPrice(request.getTicketPrice())
                .active(true)
                .build();
        show = showRepository.save(show);

        // Generate seats based on theater layout
        generateSeatsForShow(show, theater);
        return toResponse(show);
    }

    private void generateSeatsForShow(Show show, Theater theater) {
        List<ShowSeat> seats = new ArrayList<>();
        for (int row = 0; row < theater.getTotalRows(); row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int seatNum = 1; seatNum <= theater.getSeatsPerRow(); seatNum++) {
                ShowSeat seat = ShowSeat.builder()
                        .show(show)
                        .rowLabel(rowLabel)
                        .seatNumber(seatNum)
                        .status(SeatStatus.AVAILABLE)
                        .build();
                seats.add(seat);
            }
        }
        showSeatRepository.saveAll(seats);
    }

    @Transactional
    public void deleteShow(Long id) {
        Show show = findById(id);
        show.setActive(false);
        showRepository.save(show);
    }

    Show findById(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Show", id));
    }

    public ShowDto.ShowResponse toResponse(Show s) {
        ShowDto.ShowResponse r = new ShowDto.ShowResponse();
        r.setId(s.getId());
        r.setMovie(movieService.toResponse(s.getMovie()));
        r.setTheater(theaterService.toResponse(s.getTheater()));
        r.setShowDate(s.getShowDate());
        r.setShowTime(s.getShowTime());
        r.setTicketPrice(s.getTicketPrice());
        r.setActive(s.isActive());
        r.setCreatedAt(s.getCreatedAt());

        long available = showSeatRepository.countByShowIdAndStatus(s.getId(), SeatStatus.AVAILABLE);
        long total = s.getTheater().getTotalSeats() != null ? s.getTheater().getTotalSeats() : 0;
        r.setAvailableSeats(available);
        r.setTotalSeats(total);
        return r;
    }
}
