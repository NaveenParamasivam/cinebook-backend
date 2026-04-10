package com.cinebook.repository;

import com.cinebook.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.showDate = :date AND s.active = true ORDER BY s.showTime")
    List<Show> findByMovieIdAndShowDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.theater.id = :theaterId AND s.showDate = :date AND s.active = true ORDER BY s.showTime")
    List<Show> findByTheaterIdAndShowDate(@Param("theaterId") Long theaterId, @Param("date") LocalDate date);

    @Query("SELECT s FROM Show s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE AND s.active = true ORDER BY s.showDate, s.showTime")
    List<Show> findUpcomingShowsByMovie(@Param("movieId") Long movieId);

    @Query("SELECT DISTINCT s.showDate FROM Show s WHERE s.movie.id = :movieId AND s.showDate >= CURRENT_DATE AND s.active = true ORDER BY s.showDate")
    List<LocalDate> findAvailableDatesByMovie(@Param("movieId") Long movieId);

    @Query("""
SELECT s FROM Show s
JOIN FETCH s.movie
JOIN FETCH s.theater
WHERE s.movie.id = :movieId AND s.showDate = :date
""")
    List<Show> findShowsWithMovieAndTheater(Long movieId, LocalDate date);
}
