package com.example.uneti.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Cơ sở dữ liệu Room cục bộ của ứng dụng, hiện chỉ chứa bảng phim yêu thích.
 * Có thể mở rộng thêm bảng (lịch sử xem, đánh giá cá nhân...) ở các bước sau.
 */
@Database(entities = {FavoriteMovie.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract FavoriteDao favoriteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "movie_app_db"
            ).build();
        }
        return instance;
    }
}
