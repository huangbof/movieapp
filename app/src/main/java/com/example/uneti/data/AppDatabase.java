package com.example.uneti.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Cơ sở dữ liệu Room cục bộ của ứng dụng, chứa bảng phim yêu thích và phim tùy chỉnh do Admin quản lý.
 */
@Database(entities = {FavoriteMovie.class, CustomMovie.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract FavoriteDao favoriteDao();
    public abstract CustomMovieDao customMovieDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "movie_app_db"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
