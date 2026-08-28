package com.example.moviewatchlist.repository;

import com.example.moviewatchlist.model.Movie;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MovieRepository {
    private final List<Movie> movies = new ArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public Movie save(Movie movie) {
        movie.setId(nextId.getAndIncrement());
        movies.add(movie);
        return movie;
    }

    public List<Movie> findAll() {
        return List.copyOf(movies);
    }

    public Optional<Movie> findById(Long id) {
        return movies.stream().filter(movie -> movie.getId().equals(id)).findFirst();
    }
}
