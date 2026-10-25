package com.example.uneti.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.uneti.model.Movie;

/**
 * Entity đại diện cho phim do Admin tự thêm, sửa, xóa cục bộ trong app.
 */
@Entity(tableName = "custom_movies")
public class CustomMovie {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String overview;
    private String posterPath;
    private String backdropPath;
    private double voteAverage;
    private String releaseDate;
    private String videoKey;
    private String category;

    public CustomMovie(int id, String title, String overview, String posterPath, String backdropPath,
                       double voteAverage, String releaseDate, String videoKey, String category) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.videoKey = videoKey;
        this.category = category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getPosterPath() { return posterPath; }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() { return backdropPath; }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public double getVoteAverage() { return voteAverage; }
    public void setVoteAverage(double voteAverage) { this.voteAverage = voteAverage; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public String getVideoKey() { return videoKey; }
    public void setVideoKey(String videoKey) { this.videoKey = videoKey; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Movie toMovie() {
        return new Movie(
                id,
                title,
                posterPath,
                (backdropPath != null && !backdropPath.isEmpty()) ? backdropPath : posterPath,
                overview,
                releaseDate,
                voteAverage
        );
    }
}
