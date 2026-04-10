package com.cinebook.controller;

import com.cinebook.dto.MovieDto;
import com.cinebook.enums.Genre;
import com.cinebook.exception.GlobalExceptionHandler;
import com.cinebook.exception.ResourceNotFoundException;
import com.cinebook.security.JwtAuthenticationFilter;
import com.cinebook.service.impl.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovieController.class)
@Import({GlobalExceptionHandler.class, com.cinebook.config.SecurityConfig.class})
class MovieControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MovieService movieService;

    @MockBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @BeforeEach
    void passThroughJwtFilter() throws Exception {
        doAnswer(inv -> {
            inv.getArgument(2, jakarta.servlet.FilterChain.class)
                    .doFilter(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    @DisplayName("GET /movies returns ApiResponse with movie list")
    void getAllMovies_ok() throws Exception {
        MovieDto.MovieResponse r = new MovieDto.MovieResponse();
        r.setId(1L);
        r.setTitle("Interstellar");
        r.setGenre(Genre.SCI_FI);
        when(movieService.getAllActiveMovies()).thenReturn(List.of(r));

        mockMvc.perform(get("/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Interstellar"))
                .andExpect(jsonPath("$.data[0].genre").value("SCI_FI"));
    }

    @Test
    @DisplayName("GET /movies/{id} maps ResourceNotFoundException to 404 ApiResponse")
    void getMovie_notFound() throws Exception {
        when(movieService.getMovieById(99L)).thenThrow(new ResourceNotFoundException("Movie", 99L));

        mockMvc.perform(get("/movies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("POST /movies requires ADMIN role")
    void createMovie_unauthenticated_is403() throws Exception {
        MovieDto.CreateMovieRequest req = new MovieDto.CreateMovieRequest();
        req.setTitle("Inception");
        req.setGenre(Genre.SCI_FI);

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /movies validates request body and returns 400 with field errors")
    void createMovie_validationError_is400() throws Exception {
        MovieDto.CreateMovieRequest req = new MovieDto.CreateMovieRequest();
        req.setTitle(""); // @NotBlank
        req.setGenre(null); // @NotNull

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.title").exists())
                .andExpect(jsonPath("$.data.genre").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /movies returns 201 with created payload")
    void createMovie_created() throws Exception {
        MovieDto.CreateMovieRequest req = new MovieDto.CreateMovieRequest();
        req.setTitle("Inception");
        req.setGenre(Genre.SCI_FI);

        MovieDto.MovieResponse resp = new MovieDto.MovieResponse();
        resp.setId(10L);
        resp.setTitle("Inception");
        resp.setGenre(Genre.SCI_FI);
        resp.setActive(true);

        when(movieService.createMovie(any(MovieDto.CreateMovieRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Movie created"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("Inception"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /movies/{id} returns success message")
    void deleteMovie_ok() throws Exception {
        mockMvc.perform(delete("/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Movie deleted"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /movies/{id} returns updated payload")
    void updateMovie_ok() throws Exception {
        MovieDto.UpdateMovieRequest req = new MovieDto.UpdateMovieRequest();
        req.setTitle("Updated");

        MovieDto.MovieResponse resp = new MovieDto.MovieResponse();
        resp.setId(1L);
        resp.setTitle("Updated");

        when(movieService.updateMovie(eq(1L), any(MovieDto.UpdateMovieRequest.class))).thenReturn(resp);

        mockMvc.perform(put("/movies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Movie updated"))
                .andExpect(jsonPath("$.data.title").value("Updated"));
    }
}

