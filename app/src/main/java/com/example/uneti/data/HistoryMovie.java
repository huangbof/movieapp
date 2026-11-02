package com.example.uneti.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history_movies")
public class HistoryMovie {

    @PrimaryKey
    private int movieId;

    private String title;
    private String posterPath;
    private double voteAverage;
    private long viewedAt;

    public HistoryMovie(int movieId, String title, String posterPath, double voteAverage, long viewedAt) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.viewedAt = viewedAt;
    }

    public int getMovieId() { return movieId; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public double getVoteAverage() { return voteAverage; }
    public long getViewedAt() { return viewedAt; }
}

