package com.example.uneti;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.FavoriteDao;
import com.example.uneti.data.FavoriteMovie;
import com.example.uneti.model.Genre;
import com.example.uneti.model.MovieDetail;
import com.example.uneti.model.Video;
import com.example.uneti.model.VideoResponse;
import com.example.uneti.network.ApiClient;
import com.example.uneti.network.Constants;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình Chi tiết phim: hiển thị thông tin phim, cho phép xem trailer
 * (mở bằng YouTube) và thêm/xoá khỏi danh sách Yêu thích (lưu bằng Room).
 */
public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIE_ID = "movie_id";

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int movieId;
    private String trailerKey;
    private boolean isFavorite = false;
    private MovieDetail currentDetail;

    private FavoriteDao favoriteDao;

    private ImageView imgBackdrop;
    private TextView tvTitle, tvMeta, tvOverview, btnPlayTrailer, btnFavorite;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        movieId = getIntent().getIntExtra(EXTRA_MOVIE_ID, -1);
        if (movieId == -1) {
            finish();
            return;
        }

        favoriteDao = AppDatabase.getInstance(this).favoriteDao();

        imgBackdrop = findViewById(R.id.imgBackdrop);
        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvOverview = findViewById(R.id.tvOverview);
        btnPlayTrailer = findViewById(R.id.btnPlayTrailer);
        btnFavorite = findViewById(R.id.btnFavorite);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlayTrailer.setOnClickListener(v -> openTrailer());
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        loadDetail();
        loadVideos();
        checkFavoriteState();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getApi().getMovieDetail(movieId, Constants.API_KEY, "vi-VN")
                .enqueue(new Callback<MovieDetail>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieDetail> call,
                                            @NonNull Response<MovieDetail> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            bindDetail(response.body());
                        } else {
                            showLoadError();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieDetail> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showLoadError();
                    }
                });
    }

    private void bindDetail(MovieDetail detail) {
        currentDetail = detail;

        tvTitle.setText(detail.getTitle());

        String year = (detail.getReleaseDate() != null && detail.getReleaseDate().length() >= 4)
                ? detail.getReleaseDate().substring(0, 4) : "";
        String genres = joinGenres(detail.getGenres());

        StringBuilder meta = new StringBuilder();
        if (!year.isEmpty()) meta.append(year);
        if (detail.getRuntime() > 0) meta.append(meta.length() > 0 ? "  ·  " : "").append(detail.getRuntime()).append(" phút");
        meta.append(meta.length() > 0 ? "  ·  " : "").append("★ ").append(String.format("%.1f", detail.getVoteAverage()));
        if (!genres.isEmpty()) meta.append("  ·  ").append(genres);
        tvMeta.setText(meta.toString());

        tvOverview.setText(detail.getOverview() == null || detail.getOverview().isEmpty()
                ? "Chưa có mô tả cho phim này." : detail.getOverview());

        Glide.with(this)
                .load(Constants.IMAGE_BASE_URL + detail.getBackdropPath())
                .into(imgBackdrop);
    }

    private String joinGenres(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            sb.append(genres.get(i).getName());
            if (i < genres.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private void loadVideos() {
        ApiClient.getApi().getMovieVideos(movieId, Constants.API_KEY)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VideoResponse> call,
                                            @NonNull Response<VideoResponse> response) {
                        if (response.body() == null || response.body().getResults() == null) return;
                        for (Video v : response.body().getResults()) {
                            if ("YouTube".equalsIgnoreCase(v.getSite())
                                    && "Trailer".equalsIgnoreCase(v.getType())) {
                                trailerKey = v.getKey();
                                return;
                            }
                        }
                        // Không có "Trailer" chính thức -> lấy video YouTube đầu tiên tìm được
                        for (Video v : response.body().getResults()) {
                            if ("YouTube".equalsIgnoreCase(v.getSite())) {
                                trailerKey = v.getKey();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
                        // Bỏ qua: nút "Xem trailer" sẽ báo chưa tìm thấy nếu bấm vào
                    }
                });
    }

    private void openTrailer() {
        if (trailerKey == null) {
            Toast.makeText(this, "Chưa tìm thấy trailer cho phim này.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + trailerKey));
        startActivity(intent);
    }

    private void checkFavoriteState() {
        executor.execute(() -> {
            FavoriteMovie fav = favoriteDao.findById(movieId);
            isFavorite = fav != null;
            runOnUiThread(this::updateFavoriteButton);
        });
    }

    private void toggleFavorite() {
        executor.execute(() -> {
            FavoriteMovie fav = favoriteDao.findById(movieId);
            if (fav != null) {
                favoriteDao.delete(fav);
                isFavorite = false;
            } else if (currentDetail != null) {
                favoriteDao.insert(new FavoriteMovie(
                        movieId,
                        currentDetail.getTitle(),
                        currentDetail.getPosterPath(),
                        currentDetail.getVoteAverage()));
                isFavorite = true;
            }
            runOnUiThread(this::updateFavoriteButton);
        });
    }

    private void updateFavoriteButton() {
        btnFavorite.setText(isFavorite ? "♥  Đã thích" : "♡  Yêu thích");
    }

    private void showLoadError() {
        Toast.makeText(this, "Không tải được thông tin phim. Kiểm tra API Key hoặc kết nối mạng.",
                Toast.LENGTH_LONG).show();
    }
}
