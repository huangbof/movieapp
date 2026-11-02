package com.example.uneti.model;

import java.util.List;

public class MovieCategory {

    private final String title;
    private final List<Movie> movies;

    public MovieCategory(String title, List<Movie> movies) {
        this.title = title;
        this.movies = movies;
    }

    public String getTitle() {
        return title;
    }

    public List<Movie> getMovies() {
        return movies;
    }
}

