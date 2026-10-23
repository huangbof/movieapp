package com.example.uneti.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model đầy đủ cho màn hình Chi tiết phim, lấy từ endpoint GET /movie/{id}.
 */
public class MovieDetail {

    private int id;
    private String title;
    private String overview;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("backdrop_path")
    private String backdropPath;

    @SerializedName("release_date")
    private String releaseDate;

    @SerializedName("vote_average")
    private double voteAverage;

    private int runtime;

    private List<Genre> genres;

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public String getPosterPath() { return posterPath; }
    public String getBackdropPath() { return backdropPath; }
    public String getReleaseDate() { return releaseDate; }
    public double getVoteAverage() { return voteAverage; }
    public int getRuntime() { return runtime; }
    public List<Genre> getGenres() { return genres; }
}
