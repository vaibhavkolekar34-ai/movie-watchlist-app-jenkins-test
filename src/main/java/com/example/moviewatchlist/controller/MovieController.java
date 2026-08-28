package com.example.moviewatchlist.controller;

import com.example.moviewatchlist.model.Movie;
import com.example.moviewatchlist.service.MovieService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie addMovie(@RequestBody Movie movie) {
        return movieService.addMovie(movie);
    }

    @GetMapping
    public List<Movie> getMovies() {
        return movieService.getAllMovies();
    }

    @PutMapping("/{id}/watched")
    public Movie markAsWatched(@PathVariable Long id) {
        return movieService.markAsWatched(id);
    }

    @PutMapping("/{id}/rating")
    public Movie rateMovie(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return movieService.rateMovie(id, request.get("rating"));
    }
}
