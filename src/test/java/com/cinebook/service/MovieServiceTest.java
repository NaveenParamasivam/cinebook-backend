package com.cinebook.service;

import com.cinebook.dto.MovieDto;
import com.cinebook.entity.Movie;
import com.cinebook.enums.Genre;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.MovieRepository;
import com.cinebook.service.impl.MovieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        sampleMovie = Movie.builder()
                .id(1L)
                .title("Interstellar")
                .description("A journey through space and time")
                .genre(Genre.SCI_FI)
                .durationMinutes(169)
                .rating("PG-13")
                .imdbRating(8.6)
                .language("English")
                .posterUrl("https://example.com/interstellar.jpg")
                .releaseDate(LocalDate.of(2014, 11, 7))
                .director("Christopher Nolan")
                .cast("Matthew McConaughey, Anne Hathaway")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Get All Active Movies")
    class GetAllActiveMoviesTests {

        @Test
        @DisplayName("Should return all active movies")
        void shouldReturnAllActiveMovies() {
            when(movieRepository.findByActiveTrueOrderByReleaseDateDesc())
                    .thenReturn(List.of(sampleMovie));

            List<MovieDto.MovieResponse> result = movieService.getAllActiveMovies();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Interstellar");
            assertThat(result.get(0).getGenre()).isEqualTo(Genre.SCI_FI);
            verify(movieRepository).findByActiveTrueOrderByReleaseDateDesc();
        }

        @Test
        @DisplayName("Should return empty list when no movies exist")
        void shouldReturnEmptyListWhenNoMovies() {
            when(movieRepository.findByActiveTrueOrderByReleaseDateDesc()).thenReturn(List.of());

            List<MovieDto.MovieResponse> result = movieService.getAllActiveMovies();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Get Movie By ID")
    class GetMovieByIdTests {

        @Test
        @DisplayName("Should return movie when found")
        void shouldReturnMovieWhenFound() {
            when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));

            MovieDto.MovieResponse result = movieService.getMovieById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Interstellar");
            assertThat(result.getImdbRating()).isEqualTo(8.6);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when movie not found")
        void shouldThrowWhenMovieNotFound() {
            when(movieRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.getMovieById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Create Movie")
    class CreateMovieTests {

        @Test
        @DisplayName("Should create movie and return response")
        void shouldCreateMovie() {
            MovieDto.CreateMovieRequest request = new MovieDto.CreateMovieRequest();
            request.setTitle("Inception");
            request.setGenre(Genre.SCI_FI);
            request.setDurationMinutes(148);
            request.setLanguage("English");
            request.setRating("PG-13");
            request.setImdbRating(8.8);

            Movie savedMovie = Movie.builder()
                    .id(2L)
                    .title("Inception")
                    .genre(Genre.SCI_FI)
                    .durationMinutes(148)
                    .language("English")
                    .rating("PG-13")
                    .imdbRating(8.8)
                    .active(true)
                    .build();

            when(movieRepository.save(any(Movie.class))).thenReturn(savedMovie);

            MovieDto.MovieResponse result = movieService.createMovie(request);

            assertThat(result.getTitle()).isEqualTo("Inception");
            assertThat(result.getGenre()).isEqualTo(Genre.SCI_FI);
            assertThat(result.isActive()).isTrue();
            verify(movieRepository).save(any(Movie.class));
        }
    }

    @Nested
    @DisplayName("Update Movie")
    class UpdateMovieTests {

        @Test
        @DisplayName("Should update movie fields correctly")
        void shouldUpdateMovieFields() {
            when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

            MovieDto.UpdateMovieRequest request = new MovieDto.UpdateMovieRequest();
            request.setTitle("Interstellar (Remastered)");
            request.setImdbRating(9.0);

            MovieDto.MovieResponse result = movieService.updateMovie(1L, request);

            assertThat(sampleMovie.getTitle()).isEqualTo("Interstellar (Remastered)");
            assertThat(sampleMovie.getImdbRating()).isEqualTo(9.0);
            verify(movieRepository).save(sampleMovie);
        }

        @Test
        @DisplayName("Should throw when updating non-existent movie")
        void shouldThrowWhenUpdatingNonExistentMovie() {
            when(movieRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> movieService.updateMovie(99L, new MovieDto.UpdateMovieRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete Movie (Soft Delete)")
    class DeleteMovieTests {

        @Test
        @DisplayName("Should soft-delete movie by setting active = false")
        void shouldSoftDeleteMovie() {
            when(movieRepository.findById(1L)).thenReturn(Optional.of(sampleMovie));
            when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

            movieService.deleteMovie(1L);

            assertThat(sampleMovie.isActive()).isFalse();
            verify(movieRepository).save(sampleMovie);
        }
    }

    @Nested
    @DisplayName("Search Movies")
    class SearchMoviesTests {

        @Test
        @DisplayName("Should return matching movies for query")
        void shouldReturnMatchingMovies() {
            when(movieRepository.searchMovies("inter")).thenReturn(List.of(sampleMovie));

            List<MovieDto.MovieResponse> result = movieService.searchMovies("inter");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).isEqualTo("Interstellar");
        }

        @Test
        @DisplayName("Should return empty list when no matches")
        void shouldReturnEmptyWhenNoMatches() {
            when(movieRepository.searchMovies("xyz123")).thenReturn(List.of());

            List<MovieDto.MovieResponse> result = movieService.searchMovies("xyz123");

            assertThat(result).isEmpty();
        }
    }
}
