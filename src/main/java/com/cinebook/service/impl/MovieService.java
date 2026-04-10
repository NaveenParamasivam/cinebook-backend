package com.cinebook.service.impl;

import com.cinebook.dto.MovieDto;
import com.cinebook.entity.Movie;
import com.cinebook.enums.Genre;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public List<MovieDto.MovieResponse> getAllActiveMovies() {
        return movieRepository.findByActiveTrueOrderByReleaseDateDesc()
                .stream().map(this::toResponse).toList();
    }

    public List<MovieDto.MovieResponse> getMoviesByGenre(Genre genre) {
        return movieRepository.findByGenreAndActiveTrue(genre)
                .stream().map(this::toResponse).toList();
    }

    public List<MovieDto.MovieResponse> searchMovies(String query) {
        return movieRepository.searchMovies(query)
                .stream().map(this::toResponse).toList();
    }

    public MovieDto.MovieResponse getMovieById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public MovieDto.MovieResponse createMovie(MovieDto.CreateMovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .genre(request.getGenre())
                .durationMinutes(request.getDurationMinutes())
                .rating(request.getRating())
                .imdbRating(request.getImdbRating())
                .language(request.getLanguage())
                .posterUrl(request.getPosterUrl())
                .trailerUrl(request.getTrailerUrl())
                .releaseDate(request.getReleaseDate())
                .director(request.getDirector())
                .cast(request.getCast())
                .active(true)
                .build();
        return toResponse(movieRepository.save(movie));
    }

    @Transactional
    public MovieDto.MovieResponse updateMovie(Long id, MovieDto.UpdateMovieRequest request) {
        Movie movie = findById(id);
        if (request.getTitle() != null) movie.setTitle(request.getTitle());
        if (request.getDescription() != null) movie.setDescription(request.getDescription());
        if (request.getGenre() != null) movie.setGenre(request.getGenre());
        if (request.getDurationMinutes() != null) movie.setDurationMinutes(request.getDurationMinutes());
        if (request.getRating() != null) movie.setRating(request.getRating());
        if (request.getImdbRating() != null) movie.setImdbRating(request.getImdbRating());
        if (request.getLanguage() != null) movie.setLanguage(request.getLanguage());
        if (request.getPosterUrl() != null) movie.setPosterUrl(request.getPosterUrl());
        if (request.getTrailerUrl() != null) movie.setTrailerUrl(request.getTrailerUrl());
        if (request.getReleaseDate() != null) movie.setReleaseDate(request.getReleaseDate());
        if (request.getDirector() != null) movie.setDirector(request.getDirector());
        if (request.getCast() != null) movie.setCast(request.getCast());
        if (request.getActive() != null) movie.setActive(request.getActive());
        return toResponse(movieRepository.save(movie));
    }

    @Transactional
    public void deleteMovie(Long id) {
        Movie movie = findById(id);
        movie.setActive(false);
        movieRepository.save(movie);
    }

    private Movie findById(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", id));
    }

    public MovieDto.MovieResponse toResponse(Movie m) {
        MovieDto.MovieResponse r = new MovieDto.MovieResponse();
        r.setId(m.getId());
        r.setTitle(m.getTitle());
        r.setDescription(m.getDescription());
        r.setGenre(m.getGenre());
        r.setDurationMinutes(m.getDurationMinutes());
        r.setRating(m.getRating());
        r.setImdbRating(m.getImdbRating());
        r.setLanguage(m.getLanguage());
        r.setPosterUrl(m.getPosterUrl());
        r.setTrailerUrl(m.getTrailerUrl());
        r.setReleaseDate(m.getReleaseDate());
        r.setDirector(m.getDirector());
        r.setCast(m.getCast());
        r.setActive(m.isActive());
        r.setCreatedAt(m.getCreatedAt());
        return r;
    }
}
