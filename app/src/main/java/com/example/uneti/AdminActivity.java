package com.example.uneti;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.adapter.AdminMovieAdapter;
import com.example.uneti.adapter.UserAdapter;
import com.example.uneti.auth.SessionManager;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.CustomMovie;
import com.example.uneti.network.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Màn hình Bảng điều khiển Quản trị viên (Admin Panel):
 * - Thống kê số lượng tài khoản, số phim nội bộ, số phim yêu thích.
 * - Quản lý Phim nội bộ: Thêm phim mới, Sửa thông tin phim, Xóa phim.
 * - Quản lý người dùng: Thêm tài khoản, Đổi mật khẩu, Xóa tài khoản.
 * - Đăng thông báo toàn hệ thống (Broadcast Announcement) trên Trang chủ.
 * - Cấu hình / Thay đổi TMDB API Key trực tiếp.
 */
public class AdminActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    // Quản lý người dùng
    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private final List<SessionManager.UserInfo> userList = new ArrayList<>();

    // Quản lý phim nội bộ
    private RecyclerView rvCustomMovies;
    private AdminMovieAdapter movieAdapter;
    private final List<CustomMovie> customMovieList = new ArrayList<>();
    private TextView tvStatCustomMovies, tvNoCustomMovies;

    private TextView tvStatUsers, tvStatFavorites;
    private EditText etAnnouncement, etApiKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "Bạn không có quyền truy cập trang Quản trị viên!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        findViewById(R.id.btnAdminBack).setOnClickListener(v -> finish());

        tvStatUsers = findViewById(R.id.tvStatUsers);
        tvStatCustomMovies = findViewById(R.id.tvStatCustomMovies);
        tvStatFavorites = findViewById(R.id.tvStatFavorites);
        tvNoCustomMovies = findViewById(R.id.tvNoCustomMovies);
        etAnnouncement = findViewById(R.id.etAnnouncement);
        etApiKey = findViewById(R.id.etApiKey);

        // 1. Setup RecyclerView Người dùng
        rvUsers = findViewById(R.id.rvUsers);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(userList, new UserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(SessionManager.UserInfo user) {
                confirmDeleteUser(user);
            }

            @Override
            public void onResetPassword(SessionManager.UserInfo user) {
                showChangePasswordDialog(user);
            }
        });
        rvUsers.setAdapter(userAdapter);

        // 2. Setup RecyclerView Phim nội bộ do Admin quản lý
        rvCustomMovies = findViewById(R.id.rvCustomMovies);
        rvCustomMovies.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new AdminMovieAdapter(customMovieList, new AdminMovieAdapter.OnMovieActionListener() {
            @Override
            public void onEdit(CustomMovie movie) {
                showMovieDialog(movie);
            }

            @Override
            public void onDelete(CustomMovie movie) {
                confirmDeleteMovie(movie);
            }
        });
        rvCustomMovies.setAdapter(movieAdapter);

        // Các nút hành động
        findViewById(R.id.btnAddMovie).setOnClickListener(v -> showMovieDialog(null));
        findViewById(R.id.btnAddUser).setOnClickListener(v -> showAddUserDialog());
        findViewById(R.id.btnSaveAnnouncement).setOnClickListener(v -> saveAnnouncement());
        findViewById(R.id.btnClearAnnouncement).setOnClickListener(v -> clearAnnouncement());
        findViewById(R.id.btnSaveApiKey).setOnClickListener(v -> saveApiKey());

        loadData();
    }

    private void loadData() {
        // Tải danh sách người dùng
        userList.clear();
        userList.addAll(sessionManager.getAllUserList());
        userAdapter.notifyDataSetChanged();
        tvStatUsers.setText(String.valueOf(userList.size()));

        // Tải danh sách phim nội bộ và số lượng phim yêu thích
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CustomMovie> movies = AppDatabase.getInstance(this).customMovieDao().getAll();
            int favCount = AppDatabase.getInstance(this).favoriteDao().getAll().size();
            runOnUiThread(() -> {
                customMovieList.clear();
                customMovieList.addAll(movies);
                movieAdapter.notifyDataSetChanged();
                tvStatCustomMovies.setText(String.valueOf(movies.size()));
                tvStatFavorites.setText(String.valueOf(favCount));
                tvNoCustomMovies.setVisibility(movies.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });

        // Thông báo hiện tại
        etAnnouncement.setText(sessionManager.getAnnouncement());

        // API Key hiện tại
        String activeKey = sessionManager.getCustomApiKey();
        if (activeKey == null || activeKey.isEmpty()) {
            activeKey = Constants.API_KEY;
        }
        etApiKey.setText(activeKey);
    }

    // ==========================================
    // QUẢN LÝ PHIM (THÊM, SỬA, XÓA)
    // ==========================================

    private void showMovieDialog(@Nullable CustomMovie existingMovie) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_movie, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogMovieTitle);
        EditText etTitle = dialogView.findViewById(R.id.etDialogTitle);
        EditText etCategory = dialogView.findViewById(R.id.etDialogCategory);
        EditText etRating = dialogView.findViewById(R.id.etDialogRating);
        EditText etYear = dialogView.findViewById(R.id.etDialogYear);
        EditText etTrailer = dialogView.findViewById(R.id.etDialogTrailer);
        EditText etPoster = dialogView.findViewById(R.id.etDialogPoster);
        EditText etOverview = dialogView.findViewById(R.id.etDialogOverview);

        boolean isEdit = existingMovie != null;
        if (isEdit) {
            tvTitle.setText("Sửa thông tin phim");
            etTitle.setText(existingMovie.getTitle());
            etCategory.setText(existingMovie.getCategory());
            etRating.setText(String.valueOf(existingMovie.getVoteAverage()));
            etYear.setText(existingMovie.getReleaseDate());
            etTrailer.setText(existingMovie.getVideoKey());
            etPoster.setText(existingMovie.getPosterPath());
            etOverview.setText(existingMovie.getOverview());
        } else {
            tvTitle.setText("Thêm phim mới");
            etRating.setText("8.0");
            etYear.setText("2024");
            etCategory.setText("Phim Độc Quyền Nitflex");
        }

        dialogView.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDialogSave).setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Tên phim không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            String category = etCategory.getText().toString().trim();
            String year = etYear.getText().toString().trim();
            String trailerInput = etTrailer.getText().toString().trim();
            String videoKey = extractYouTubeKey(trailerInput);
            String poster = etPoster.getText().toString().trim();
            String overview = etOverview.getText().toString().trim();

            double rating = 8.0;
            try {
                rating = Double.parseDouble(etRating.getText().toString().trim());
            } catch (Exception ignored) {
            }

            double finalRating = rating;
            Executors.newSingleThreadExecutor().execute(() -> {
                if (isEdit) {
                    existingMovie.setTitle(title);
                    existingMovie.setCategory(category);
                    existingMovie.setReleaseDate(year);
                    existingMovie.setVoteAverage(finalRating);
                    existingMovie.setVideoKey(videoKey);
                    existingMovie.setPosterPath(poster);
                    existingMovie.setOverview(overview);
                    AppDatabase.getInstance(this).customMovieDao().update(existingMovie);
                } else {
                    CustomMovie newMovie = new CustomMovie(
                            0, // autoGenerate id
                            title,
                            overview,
                            poster,
                            poster, // backdrop
                            finalRating,
                            year,
                            videoKey,
                            category
                    );
                    AppDatabase.getInstance(this).customMovieDao().insert(newMovie);
                }

                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, isEdit ? "Cập nhật phim thành công!" : "Đã thêm phim mới thành công!", Toast.LENGTH_SHORT).show();
                    loadData();
                });
            });
        });

        dialog.show();
    }

    private void confirmDeleteMovie(CustomMovie movie) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa phim")
                .setMessage("Bạn có chắc chắn muốn xóa phim \"" + movie.getTitle() + "\" khỏi ứng dụng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(this).customMovieDao().delete(movie);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Đã xóa phim: " + movie.getTitle(), Toast.LENGTH_SHORT).show();
                            loadData();
                        });
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private String extractYouTubeKey(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        input = input.trim();
        if (input.contains("v=")) {
            int idx = input.indexOf("v=");
            String key = input.substring(idx + 2);
            int amp = key.indexOf("&");
            if (amp != -1) key = key.substring(0, amp);
            return key;
        } else if (input.contains("youtu.be/")) {
            int idx = input.indexOf("youtu.be/");
            String key = input.substring(idx + 9);
            int q = key.indexOf("?");
            if (q != -1) key = key.substring(0, q);
            return key;
        }
        return input;
    }

    // ==========================================
    // QUẢN LÝ NGƯỜI DÙNG
    // ==========================================

    private void showAddUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thêm tài khoản mới");

        final EditText inputUser = new EditText(this);
        inputUser.setHint("Tên đăng nhập");
        inputUser.setTextColor(ContextCompat.getColor(this, R.color.white));
        inputUser.setHintTextColor(ContextCompat.getColor(this, R.color.textMuted));

        final EditText inputPass = new EditText(this);
        inputPass.setHint("Mật khẩu");
        inputPass.setTextColor(ContextCompat.getColor(this, R.color.white));
        inputPass.setHintTextColor(ContextCompat.getColor(this, R.color.textMuted));

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        layout.addView(inputUser);
        layout.addView(inputPass);
        builder.setView(layout);

        builder.setPositiveButton("Tạo", (dialog, which) -> {
            String u = inputUser.getText().toString().trim();
            String p = inputPass.getText().toString().trim();
            if (u.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Không được để trống thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionManager.register(u, p)) {
                Toast.makeText(this, "Đã tạo tài khoản thành công!", Toast.LENGTH_SHORT).show();
                loadData();
            } else {
                Toast.makeText(this, "Tên tài khoản đã tồn tại!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void confirmDeleteUser(SessionManager.UserInfo user) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa tài khoản")
                .setMessage("Bạn có chắc chắn muốn xóa tài khoản \"" + user.username + "\" không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (sessionManager.deleteUser(user.username)) {
                        Toast.makeText(this, "Đã xóa tài khoản " + user.username, Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(this, "Không thể xóa tài khoản này!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showChangePasswordDialog(SessionManager.UserInfo user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi mật khẩu: " + user.username);

        final EditText inputPass = new EditText(this);
        inputPass.setHint("Nhập mật khẩu mới");
        inputPass.setTextColor(ContextCompat.getColor(this, R.color.white));
        inputPass.setHintTextColor(ContextCompat.getColor(this, R.color.textMuted));

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 10);
        layout.addView(inputPass);
        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newP = inputPass.getText().toString().trim();
            if (newP.isEmpty()) {
                Toast.makeText(this, "Mật khẩu không được để trống!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (sessionManager.updateUserPassword(user.username, newP)) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // ==========================================
    // THÔNG BÁO & CẤU HÌNH
    // ==========================================

    private void saveAnnouncement() {
        String msg = etAnnouncement.getText().toString().trim();
        sessionManager.setAnnouncement(msg);
        Toast.makeText(this, "Đã lưu thông báo hệ thống!", Toast.LENGTH_SHORT).show();
    }

    private void clearAnnouncement() {
        sessionManager.setAnnouncement("");
        etAnnouncement.setText("");
        Toast.makeText(this, "Đã gỡ thông báo hệ thống!", Toast.LENGTH_SHORT).show();
    }

    private void saveApiKey() {
        String key = etApiKey.getText().toString().trim();
        if (key.isEmpty()) {
            Toast.makeText(this, "API Key không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }
        sessionManager.setCustomApiKey(key);
        Toast.makeText(this, "Đã lưu API Key mới thành công!", Toast.LENGTH_SHORT).show();
    }
}
