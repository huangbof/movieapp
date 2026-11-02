package com.example.uneti;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uneti.adapter.CastAdapter;
import com.example.uneti.adapter.ReviewAdapter;
import com.example.uneti.auth.SessionManager;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.CustomMovie;
import com.example.uneti.data.FavoriteDao;
import com.example.uneti.data.FavoriteMovie;
import com.example.uneti.data.HistoryDao;
import com.example.uneti.data.HistoryMovie;
import com.example.uneti.data.MovieReview;
import com.example.uneti.data.ReviewDao;
import com.example.uneti.model.CreditsResponse;
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
 * Màn hình Chi tiết phim:
 * - Hiển thị thông tin phim, danh sách diễn viên (Credits)
 * - Tự động lưu vào Lịch sử xem phim (Room DB)
 * - Phát trailer qua WebView
 * - Thêm/xoá phim khỏi Yêu thích (Room DB)
 * - Đánh giá sao và viết bình luận phim của người dùng (Room DB)
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
    private HistoryDao historyDao;
    private ReviewDao reviewDao;

    private ImageView imgBackdrop;
    private TextView tvTitle, tvMeta, tvOverview, btnPlayTrailer, btnFavorite, btnShare;
    private ProgressBar progressBar;
    private View layoutCast;
    private RecyclerView rvCast;

    // Review components
    private final TextView[] starViews = new TextView[5];
    private int currentRating = 5;
    private TextView tvSelectedRating, btnSubmitReview, tvEmptyReviews;
    private EditText etComment;
    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        movieId = getIntent().getIntExtra(EXTRA_MOVIE_ID, -1);
        if (movieId == -1) {
            finish();
            return;
        }

        AppDatabase db = AppDatabase.getInstance(this);
        favoriteDao = db.favoriteDao();
        historyDao = db.historyDao();
        reviewDao = db.reviewDao();

        imgBackdrop = findViewById(R.id.imgBackdrop);
        tvTitle = findViewById(R.id.tvTitle);
        tvMeta = findViewById(R.id.tvMeta);
        tvOverview = findViewById(R.id.tvOverview);
        btnPlayTrailer = findViewById(R.id.btnPlayTrailer);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnShare = findViewById(R.id.btnShare);
        progressBar = findViewById(R.id.progressBar);
        layoutCast = findViewById(R.id.layoutCast);
        rvCast = findViewById(R.id.rvCast);
        rvCast.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnPlayTrailer.setOnClickListener(v -> openTrailer());
        btnFavorite.setOnClickListener(v -> toggleFavorite());
        btnShare.setOnClickListener(v -> shareMovie());

        setupReviewSection();

        loadDetail();
        loadVideos();
        loadCredits();
        checkFavoriteState();
        loadReviews();
    }

    private void setupReviewSection() {
        starViews[0] = findViewById(R.id.star1);
        starViews[1] = findViewById(R.id.star2);
        starViews[2] = findViewById(R.id.star3);
        starViews[3] = findViewById(R.id.star4);
        starViews[4] = findViewById(R.id.star5);

        tvSelectedRating = findViewById(R.id.tvSelectedRating);
        etComment = findViewById(R.id.etComment);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        tvEmptyReviews = findViewById(R.id.tvEmptyReviews);
        rvReviews = findViewById(R.id.rvReviews);

        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter();
        rvReviews.setAdapter(reviewAdapter);

        for (int i = 0; i < 5; i++) {
            final int starIndex = i + 1;
            starViews[i].setOnClickListener(v -> setStarRating(starIndex));
        }
        setStarRating(5);

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void setStarRating(int rating) {
        currentRating = rating;
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                starViews[i].setText("⭐");
            } else {
                starViews[i].setText("☆");
            }
        }
        if (tvSelectedRating != null) {
            tvSelectedRating.setText(rating + "/5 sao");
        }
    }

    private void submitReview() {
        String comment = etComment.getText().toString().trim();
        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        SessionManager sessionManager = new SessionManager(this);
        String username = sessionManager.getCurrentUser();
        if (username == null || username.isEmpty()) {
            username = "Khách";
        }

        MovieReview review = new MovieReview(
                movieId,
                username,
                currentRating,
                comment,
                System.currentTimeMillis()
        );

        executor.execute(() -> {
            reviewDao.insert(review);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                etComment.setText("");
                setStarRating(5);
                loadReviews();
            });
        });
    }

    private void loadReviews() {
        executor.execute(() -> {
            List<MovieReview> list = reviewDao.getReviewsByMovie(movieId);
            runOnUiThread(() -> {
                reviewAdapter.setData(list);
                if (list.isEmpty()) {
                    tvEmptyReviews.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyReviews.setVisibility(View.GONE);
                }
            });
        });
    }

    private void saveToHistory(String title, String posterPath, double voteAverage) {
        executor.execute(() -> {
            historyDao.insert(new HistoryMovie(
                    movieId,
                    title,
                    posterPath,
                    voteAverage,
                    System.currentTimeMillis()
            ));
        });
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

        // Lưu vào lịch sử xem
        saveToHistory(custom.getTitle(), custom.getPosterPath(), custom.getVoteAverage());
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

        // Lưu vào lịch sử xem
        saveToHistory(detail.getTitle(), detail.getPosterPath(), detail.getVoteAverage());
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
                        for (Video v : response.body().getResults()) {
                            if ("YouTube".equalsIgnoreCase(v.getSite())) {
                                trailerKey = v.getKey();
                                return;
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
                    }
                });
    }

    private void loadCredits() {
        if (movieId <= 0) return;
        ApiClient.getApi().getMovieCredits(movieId, Constants.getApiKey(this), "vi-VN")
                .enqueue(new Callback<CreditsResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<CreditsResponse> call,
                                           @NonNull Response<CreditsResponse> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getCast() == null
                                || response.body().getCast().isEmpty()) return;

                        List<com.example.uneti.model.Cast> castList = response.body().getCast();
                        int limit = Math.min(castList.size(), 15);
                        List<com.example.uneti.model.Cast> limitedCast = new java.util.ArrayList<>(castList.subList(0, limit));

                        runOnUiThread(() -> {
                            rvCast.setAdapter(new CastAdapter(limitedCast));
                            layoutCast.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<CreditsResponse> call, @NonNull Throwable t) {
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
