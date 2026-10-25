package com.example.uneti.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CustomMovieDao {

    @Query("SELECT * FROM custom_movies ORDER BY id DESC")
    List<CustomMovie> getAll();

    @Query("SELECT * FROM custom_movies WHERE id = :id LIMIT 1")
    CustomMovie findById(int id);

    @Query("SELECT * FROM custom_movies WHERE title LIKE '%' || :query || '%'")
    List<CustomMovie> searchByTitle(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CustomMovie movie);

    @Update
    void update(CustomMovie movie);

    @Delete
    void delete(CustomMovie movie);

    @Query("SELECT COUNT(*) FROM custom_movies")
    int count();
}
