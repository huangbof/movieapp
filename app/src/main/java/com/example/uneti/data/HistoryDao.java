package com.example.uneti.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HistoryMovie movie);

    @Query("SELECT * FROM history_movies ORDER BY viewedAt DESC")
    List<HistoryMovie> getAll();

    @Query("DELETE FROM history_movies WHERE movieId = :id")
    void delete(int id);

    @Query("DELETE FROM history_movies")
    void clearAll();
}
