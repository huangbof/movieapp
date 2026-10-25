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
import com.example.uneti.data.CustomMovie;
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
    private CustomMovie currentCustomMovie;

    private FavoriteDao favoriteDao;

    private ImageView imgBackdrop;
    private TextView tvTitle, tvMeta, tvOverview, btnPlayTrailer, btnFavorite, btnShare;
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
        btnShare = findViewById(R.id.btnShare);
        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlayTrailer.setOnClickListener(v -> openTrailer());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareMovie());

        loadDetail();
        loadVideos();
        checkFavoriteState();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            CustomMovie custom = AppDatabase.getInstance(this).customMovieDao().findById(movieId);
            if (custom != null) {
                currentCustomMovie = custom;
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    bindCustomDetail(custom);
                });
            } else {
                runOnUiThread(this::loadDetailFromTmdb);
            }
        });
    }

    private void loadDetailFromTmdb() {
        ApiClient.getApi().getMovieDetail(movieId, Constants.getApiKey(this), "vi-VN")
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

    private void bindCustomDetail(CustomMovie custom) {
        tvTitle.setText(custom.getTitle());

        StringBuilder meta = new StringBuilder();
        if (custom.getReleaseDate() != null && !custom.getReleaseDate().isEmpty()) {
            meta.append(custom.getReleaseDate());
        }
        meta.append(meta.length() > 0 ? "  ·  " : "").append("★ ").append(String.format("%.1f", custom.getVoteAverage()));
        if (custom.getCategory() != null && !custom.getCategory().isEmpty()) {
            meta.append("  ·  ").append(custom.getCategory());
        }
        tvMeta.setText(meta.toString());

        tvOverview.setText(custom.getOverview() == null || custom.getOverview().isEmpty()
                ? "Chưa có mô tả cho phim này." : custom.getOverview());

        trailerKey = custom.getVideoKey();

        String backdrop = custom.getBackdropPath();
        if (backdrop == null || backdrop.isEmpty()) backdrop = custom.getPosterPath();
        String backdropUrl = (backdrop != null && (backdrop.startsWith("http://") || backdrop.startsWith("https://")))
                ? backdrop : (Constants.IMAGE_BASE_URL + backdrop);

        Glide.with(this).load(backdropUrl).into(imgBackdrop);
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
        ApiClient.getApi().getMovieVideos(movieId, Constants.getApiKey(this))
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
        Intent intent = new Intent(this, TrailerPlayerActivity.class);
        intent.putExtra(TrailerPlayerActivity.EXTRA_VIDEO_KEY, trailerKey);
        String title = currentDetail != null ? currentDetail.getTitle()
                : (currentCustomMovie != null ? currentCustomMovie.getTitle() : "Trailer");
        intent.putExtra(TrailerPlayerActivity.EXTRA_MOVIE_TITLE, title);
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
            } else if (currentCustomMovie != null) {
                favoriteDao.insert(new FavoriteMovie(
                        movieId,
                        currentCustomMovie.getTitle(),
                        currentCustomMovie.getPosterPath(),
                        currentCustomMovie.getVoteAverage()));
                isFavorite = true;
            }
            runOnUiThread(this::updateFavoriteButton);
        });
    }

    private void updateFavoriteButton() {
        btnFavorite.setText(isFavorite ? "♥  Đã thích" : "♡  Yêu thích");
    }

    private void shareMovie() {
        String title = currentDetail != null ? currentDetail.getTitle()
                : (currentCustomMovie != null ? currentCustomMovie.getTitle() : null);
        if (title == null) return;
        double rating = currentDetail != null ? currentDetail.getVoteAverage()
                : (currentCustomMovie != null ? currentCustomMovie.getVoteAverage() : 0);

        String shareText = "🎬 " + title
                + "\n⭐ " + String.format("%.1f", rating)
                + "\n\nXem thêm trên Nitflex!";
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Chia sẻ phim qua..."));
    }

    private void showLoadError() {
        Toast.makeText(this, "Không tải được thông tin phim. Kiểm tra API Key hoặc kết nối mạng.",
                Toast.LENGTH_LONG).show();
    }
}
