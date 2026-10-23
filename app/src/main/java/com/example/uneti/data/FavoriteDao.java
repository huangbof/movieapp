package com.example.uneti.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteMovie movie);

    @Delete
    void delete(FavoriteMovie movie);

    @Query("SELECT * FROM favorite_movies ORDER BY id DESC")
    List<FavoriteMovie> getAll();

    @Query("SELECT * FROM favorite_movies WHERE id = :movieId LIMIT 1")
    FavoriteMovie findById(int movieId);
}
