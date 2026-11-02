package com.example.uneti.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReviewDao {

    @Insert
    void insert(MovieReview review);

    @Query("SELECT * FROM movie_reviews WHERE movieId = :movieId ORDER BY createdAt DESC")
    List<MovieReview> getReviewsByMovie(int movieId);

    @Query("SELECT AVG(rating) FROM movie_reviews WHERE movieId = :movieId")
    Float getAverageRating(int movieId);

    @Delete
    void delete(MovieReview review);
}
