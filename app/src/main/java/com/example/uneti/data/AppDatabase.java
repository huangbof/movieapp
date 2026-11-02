package com.example.uneti.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.Executors;

/**
 * Cơ sở dữ liệu Room cục bộ của ứng dụng Nitflex, bao gồm 5 bảng:
 * - User: Quản lý tài khoản người dùng và phân quyền Admin
 * - FavoriteMovie: Danh sách phim yêu thích
 * - CustomMovie: Phim tùy chỉnh do Admin quản lý
 * - HistoryMovie: Lịch sử xem phim của người dùng
 * - MovieReview: Đánh giá và bình luận phim
 */
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

            // Khởi tạo tài khoản quản trị mặc định nếu chưa tồn tại
            Executors.newSingleThreadExecutor().execute(() -> {
                if (instance.userDao().findByUsername("admin") == null) {
                    instance.userDao().insert(new User("admin", "admin123", true, System.currentTimeMillis()));
                }
            });
        }
        return instance;
    }
}
