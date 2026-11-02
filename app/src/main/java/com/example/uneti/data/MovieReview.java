package com.example.uneti.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity Room đại diện cho một Đánh giá / Bình luận phim của người dùng.
 */
@Entity(tableName = "movie_reviews")
public class MovieReview {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int movieId;
    private String username;
    private float rating;
    private String comment;
    private long createdAt;

    public MovieReview(int movieId, String username, float rating, String comment, long createdAt) {
        this.movieId = movieId;
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMovieId() { return movieId; }
    public String getUsername() { return username; }
    public float getRating() { return rating; }
    public String getComment() { return comment; }
    public long getCreatedAt() { return createdAt; }
}
