package com.example.uneti.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model đại diện cho phần phản hồi (response) của API danh sách phim
 * (ví dụ: GET /movie/popular).
 */
public class MovieResponse {

    private int page;
    private List<Movie> results;

    @SerializedName("total_pages")
    private int totalPages;

    @SerializedName("total_results")
    private int totalResults;

    public int getPage() {
        return page;
    }

    public List<Movie> getResults() {
        return results;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getTotalResults() {
        return totalResults;
    }
}
