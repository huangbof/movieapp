package com.example.uneti.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.Executors;

@Database(
        entities = {User.class, FavoriteMovie.class, CustomMovie.class, HistoryMovie.class, MovieReview.class},
        version = 4,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();
    public abstract FavoriteDao favoriteDao();
    public abstract CustomMovieDao customMovieDao();
    public abstract HistoryDao historyDao();
    public abstract ReviewDao reviewDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "movie_app_db"
            ).fallbackToDestructiveMigration()
             .allowMainThreadQueries()
             .build();

            Executors.newSingleThreadExecutor().execute(() -> {
                if (instance.userDao().findByUsername("admin") == null) {
                    instance.userDao().insert(new User("admin", "admin123", true, System.currentTimeMillis()));
                }
            });
        }
        return instance;
    }
}

