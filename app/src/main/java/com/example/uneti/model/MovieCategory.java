package com.example.uneti.model;

import java.util.List;

/**
 * Đại diện cho một hàng phim theo danh mục trên Trang chủ
 * (ví dụ: "Thịnh hành", "Đề cử hàng đầu", "Đang chiếu", "Sắp chiếu"),
 * mô phỏng theo cách bố trí trang chủ của Netflix / FPT Play.
 */
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
