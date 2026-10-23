package com.example.uneti.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity Room đại diện cho một phim trong danh sách Yêu thích,
 * lưu cục bộ trên máy (SQLite thông qua Room).
 */
@Entity(tableName = "favorite_movies")
public class FavoriteMovie {

    @PrimaryKey
    private int id;

    private String title;
    private String posterPath;
    private double voteAverage;

    public FavoriteMovie(int id, String title, String posterPath, double voteAverage) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getPosterPath() { return posterPath; }
    public double getVoteAverage() { return voteAverage; }
}
