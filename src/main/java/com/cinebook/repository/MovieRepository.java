package com.cinebook.repository;

import com.cinebook.entity.Movie;
import com.cinebook.enums.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByActiveTrueOrderByReleaseDateDesc();
    List<Movie> findByGenreAndActiveTrue(Genre genre);

    @Query("SELECT m FROM Movie m WHERE m.active = true AND " +
           "(LOWER(m.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(m.genre) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Movie> searchMovies(@Param("q") String q);
}
