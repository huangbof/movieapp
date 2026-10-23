package com.example.uneti.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uneti.DetailActivity;
import com.example.uneti.R;
import com.example.uneti.adapter.CategoryAdapter;
import com.example.uneti.model.Movie;
import com.example.uneti.model.MovieCategory;
import com.example.uneti.model.MovieResponse;
import com.example.uneti.model.Video;
import com.example.uneti.model.VideoResponse;
import com.example.uneti.network.ApiClient;
import com.example.uneti.network.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình Trang chủ theo phong cách Netflix / FPT Play:
 * - Banner lớn giới thiệu 1 phim nổi bật ở trên cùng.
 * - Bên dưới là các hàng phim cuộn ngang theo danh mục
 *   (Thịnh hành, Đề cử hàng đầu, Đang chiếu, Sắp chiếu),
 *   mỗi hàng lấy dữ liệu từ một endpoint khác nhau của TMDB.
 */
public class HomeFragment extends Fragment {

    private static final String LANGUAGE = "vi-VN";

    private ImageView imgHeroBackdrop;
    private TextView tvHeroTitle;
    private RecyclerView recyclerViewCategories;
    private ProgressBar progressBar;

    private final List<MovieCategory> categories = new ArrayList<>();
    private int pendingRequests = 0;
    private int heroMovieId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgHeroBackdrop = view.findViewById(R.id.imgHeroBackdrop);
        tvHeroTitle = view.findViewById(R.id.tvHeroTitle);
        recyclerViewCategories = view.findViewById(R.id.recyclerViewCategories);
        progressBar = view.findViewById(R.id.progressBar);

        View btnPlay = view.findViewById(R.id.btnPlay);
        View btnInfo = view.findViewById(R.id.btnInfo);
        btnPlay.setOnClickListener(v -> playHeroTrailer());
        btnInfo.setOnClickListener(v -> openHeroDetail());

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        loadHomeData();
    }

    private void loadHomeData() {
        progressBar.setVisibility(View.VISIBLE);
        categories.clear();

        // Mỗi "slot" giữ đúng vị trí hàng để thứ tự không bị xáo trộn
        // khi các request trả về không đồng thời.
        categories.add(new MovieCategory("Thịnh hành", new ArrayList<>()));
        categories.add(new MovieCategory("Đề cử hàng đầu", new ArrayList<>()));
        categories.add(new MovieCategory("Đang chiếu", new ArrayList<>()));
        categories.add(new MovieCategory("Sắp chiếu", new ArrayList<>()));

        pendingRequests = categories.size();

        fetchInto(0, ApiClient.getApi().getPopularMovies(Constants.API_KEY, LANGUAGE, 1));
        fetchInto(1, ApiClient.getApi().getTopRatedMovies(Constants.API_KEY, LANGUAGE, 1));
        fetchInto(2, ApiClient.getApi().getNowPlayingMovies(Constants.API_KEY, LANGUAGE, 1));
        fetchInto(3, ApiClient.getApi().getUpcomingMovies(Constants.API_KEY, LANGUAGE, 1));
    }

    private void fetchInto(int slotIndex, Call<MovieResponse> call) {
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieResponse> call,
                                    @NonNull Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getResults() != null) {
                    List<Movie> movies = response.body().getResults();
                    categories.set(slotIndex,
                            new MovieCategory(categories.get(slotIndex).getTitle(), movies));

                    // Banner lớn lấy phim đầu tiên của hàng "Thịnh hành"
                    if (slotIndex == 0 && !movies.isEmpty()) {
                        bindHero(movies.get(0));
                    }
                }
                onOneRequestDone();
            }

            @Override
            public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                onOneRequestDone();
            }
        });
    }

    private void bindHero(Movie movie) {
        if (getContext() == null) return;
        heroMovieId = movie.getId();
        tvHeroTitle.setText(movie.getTitle());
        String backdropUrl = Constants.IMAGE_BASE_URL + movie.getBackdropPath();
        Glide.with(getContext()).load(backdropUrl).into(imgHeroBackdrop);
    }

    private void openHeroDetail() {
        if (heroMovieId == -1 || getContext() == null) return;
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, heroMovieId);
        startActivity(intent);
    }

    private void playHeroTrailer() {
        if (heroMovieId == -1 || getContext() == null) return;
        ApiClient.getApi().getMovieVideos(heroMovieId, Constants.API_KEY)
                .enqueue(new Callback<VideoResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<VideoResponse> call,
                                            @NonNull Response<VideoResponse> response) {
                        String key = null;
                        if (response.body() != null && response.body().getResults() != null) {
                            for (Video v : response.body().getResults()) {
                                if ("YouTube".equalsIgnoreCase(v.getSite())) {
                                    key = v.getKey();
                                    if ("Trailer".equalsIgnoreCase(v.getType())) break;
                                }
                            }
                        }
                        if (key != null && getContext() != null) {
                            startActivity(new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://www.youtube.com/watch?v=" + key)));
                        } else if (getContext() != null) {
                            Toast.makeText(getContext(), "Chưa tìm thấy trailer cho phim này.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<VideoResponse> call, @NonNull Throwable t) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Không tải được trailer. Kiểm tra kết nối mạng.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void onOneRequestDone() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            progressBar.setVisibility(View.GONE);
            recyclerViewCategories.setAdapter(new CategoryAdapter(categories));

            boolean allEmpty = true;
            for (MovieCategory c : categories) {
                if (!c.getMovies().isEmpty()) {
                    allEmpty = false;
                    break;
                }
            }
            if (allEmpty && getContext() != null) {
                Toast.makeText(getContext(),
                        "Không tải được dữ liệu phim. Kiểm tra API Key (Constants.java) hoặc kết nối mạng.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
