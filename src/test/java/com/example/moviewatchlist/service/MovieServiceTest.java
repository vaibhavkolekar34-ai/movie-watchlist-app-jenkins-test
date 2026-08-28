package com.example.moviewatchlist.service;

import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovieServiceTest {
    private MovieService movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(new MovieRepository());
    }

    @Test
    void shouldAddMovie() {
        Movie movie = movieService.addMovie(new Movie(null, "Inception", "Sci-Fi"));

        assertEquals(1L, movie.getId());
        assertEquals("Inception", movie.getTitle());
    }

    @Test
    void shouldGetAllMovies() {
        movieService.addMovie(new Movie(null, "Inception", "Sci-Fi"));
        movieService.addMovie(new Movie(null, "Arrival", "Sci-Fi"));

        List<Movie> movies = movieService.getAllMovies();

        assertEquals(2, movies.size());
    }

    @Test
    void shouldMarkMovieAsWatched() {
        Movie movie = movieService.addMovie(new Movie(null, "Inception", "Sci-Fi"));

        Movie updatedMovie = movieService.markAsWatched(movie.getId());

        assertTrue(updatedMovie.isWatched());
    }

    @Test
    void shouldRateMovie() {
        Movie movie = movieService.addMovie(new Movie(null, "Inception", "Sci-Fi"));

        Movie updatedMovie = movieService.rateMovie(movie.getId(), 5);

        assertEquals(5, updatedMovie.getRating());
    }

    @Test
    void shouldRejectInvalidRating() {
        Movie movie = movieService.addMovie(new Movie(null, "Inception", "Sci-Fi"));

        assertThrows(IllegalArgumentException.class, () -> movieService.rateMovie(movie.getId(), 6));
        assertFalse(movie.isWatched());
    }

    @Test
    void shouldThrowExceptionForUnknownMovie() {
        assertThrows(IllegalArgumentException.class, () -> movieService.markAsWatched(99L));
    }
}
