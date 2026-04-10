package com.cinebook.repository;

import com.cinebook.entity.Movie;
import com.cinebook.enums.Genre;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MovieRepositoryTest {

    @Autowired
    MovieRepository movieRepository;

    @Test
    @DisplayName("searchMovies matches by title and is case-insensitive")
    void searchMovies_byTitle() {
        movieRepository.save(Movie.builder().title("Interstellar").genre(Genre.SCI_FI).active(true).build());
        movieRepository.save(Movie.builder().title("Inception").genre(Genre.SCI_FI).active(true).build());
        movieRepository.save(Movie.builder().title("Some Other").genre(Genre.DRAMA).active(true).build());
        movieRepository.save(Movie.builder().title("Inactive Movie").genre(Genre.SCI_FI).active(false).build());

        List<Movie> result = movieRepository.searchMovies("inter");

        assertThat(result).extracting(Movie::getTitle).containsExactly("Interstellar");
    }

    @Test
    @DisplayName("findByActiveTrueOrderByReleaseDateDesc returns only active movies")
    void findActiveOnly() {
        movieRepository.save(Movie.builder().title("A").genre(Genre.ACTION).active(false).build());
        movieRepository.save(Movie.builder().title("B").genre(Genre.ACTION).active(true).build());

        List<Movie> result = movieRepository.findByActiveTrueOrderByReleaseDateDesc();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("B");
    }
}

