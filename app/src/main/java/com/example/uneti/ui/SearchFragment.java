package com.example.uneti.ui;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.adapter.MovieAdapter;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.CustomMovie;
import com.example.uneti.model.Movie;
import com.example.uneti.model.MovieResponse;
import com.example.uneti.network.ApiClient;
import com.example.uneti.network.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String GENRE_ACTION = "28";
    private static final String GENRE_COMEDY = "35";
    private static final String GENRE_HORROR = "27";
    private static final String GENRE_ROMANCE = "10749";
    private static final String GENRE_SCIFI = "878";
    private static final String GENRE_ANIMATION = "16";
    private static final String GENRE_THRILLER = "53";
    private static final String GENRE_DOCUMENTARY = "99";

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty, tvSearchLabel;
    private ProgressBar progressBar;

    private TextView chipAll, chipAction, chipComedy, chipHorror, chipRomance;
    private TextView chipSciFi, chipAnimation, chipThriller, chipDocumentary, chipTopRated;
    private TextView activeChip;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerViewResults);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        tvSearchLabel = view.findViewById(R.id.tvSearchLabel);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        chipAll = view.findViewById(R.id.chipAll);
        chipAction = view.findViewById(R.id.chipAction);
        chipComedy = view.findViewById(R.id.chipComedy);
        chipHorror = view.findViewById(R.id.chipHorror);
        chipRomance = view.findViewById(R.id.chipRomance);
        chipSciFi = view.findViewById(R.id.chipSciFi);
        chipAnimation = view.findViewById(R.id.chipAnimation);
        chipThriller = view.findViewById(R.id.chipThriller);
        chipDocumentary = view.findViewById(R.id.chipDocumentary);
        chipTopRated = view.findViewById(R.id.chipTopRated);
        activeChip = chipAll;

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> doSearch());
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                doSearch();
                return true;
            }
            return false;
        });

        chipAll.setOnClickListener(v -> { setActiveChip(chipAll); loadPopular(); });
        chipAction.setOnClickListener(v -> { setActiveChip(chipAction); loadByGenre(GENRE_ACTION, "💥 Hành động"); });
        chipComedy.setOnClickListener(v -> { setActiveChip(chipComedy); loadByGenre(GENRE_COMEDY, "😂 Hài hước"); });
        chipHorror.setOnClickListener(v -> { setActiveChip(chipHorror); loadByGenre(GENRE_HORROR, "👻 Kinh dị"); });
        chipRomance.setOnClickListener(v -> { setActiveChip(chipRomance); loadByGenre(GENRE_ROMANCE, "💖 Tình cảm"); });
        chipSciFi.setOnClickListener(v -> { setActiveChip(chipSciFi); loadByGenre(GENRE_SCIFI, "🚀 Viễn tưởng"); });
        chipAnimation.setOnClickListener(v -> { setActiveChip(chipAnimation); loadByGenre(GENRE_ANIMATION, "🎨 Hoạt hình"); });
        chipThriller.setOnClickListener(v -> { setActiveChip(chipThriller); loadByGenre(GENRE_THRILLER, "🔪 Giật gân"); });
        chipDocumentary.setOnClickListener(v -> { setActiveChip(chipDocumentary); loadByGenre(GENRE_DOCUMENTARY, "🎥 Tài liệu"); });
        chipTopRated.setOnClickListener(v -> { setActiveChip(chipTopRated); loadTopRated(); });

        loadPopular();
    }

    private void setActiveChip(TextView chip) {
        if (activeChip != null && activeChip != chipAll) {
            activeChip.setBackgroundResource(R.drawable.bg_chip_prompt);
            activeChip.setTextColor(getResources().getColor(R.color.textPrimary, null));
        }
        activeChip = chip;
        if (chip != chipAll) {
            chip.setBackgroundResource(R.drawable.bg_btn_play);
            chip.setTextColor(getResources().getColor(android.R.color.black, null));
        }
    }

    private void loadPopular() {
        tvSearchLabel.setText("🔥 Phim thịnh hành");
        showLoading(true);
        ApiClient.getApi().getPopularMovies(Constants.getApiKey(getContext()), "vi-VN", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null) {
                            showResults(response.body().getResults());
                        } else {
                            showEmpty("Không tải được danh sách phim.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        showEmpty("Lỗi kết nối. Vui lòng kiểm tra mạng.");
                    }
                });
    }

    private void loadByGenre(String genreId, String label) {
        tvSearchLabel.setText(label);
        showLoading(true);
        ApiClient.getApi().discoverMovies(Constants.getApiKey(getContext()), "vi-VN", genreId, "popularity.desc", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null) {
                            showResults(response.body().getResults());
                        } else {
                            showEmpty("Không tìm được phim theo thể loại này.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        showEmpty("Lỗi kết nối. Vui lòng kiểm tra mạng.");
                    }
                });
    }

    private void loadTopRated() {
        tvSearchLabel.setText("🏆 Phim điểm cao nhất");
        showLoading(true);
        ApiClient.getApi().getTopRatedMovies(Constants.getApiKey(getContext()), "vi-VN", 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                           @NonNull Response<MovieResponse> response) {
                        showLoading(false);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null) {
                            showResults(response.body().getResults());
                        } else {
                            showEmpty("Không tải được danh sách phim.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        showEmpty("Lỗi kết nối. Vui lòng kiểm tra mạng.");
                    }
                });
    }

    private void doSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên phim cần tìm.", Toast.LENGTH_SHORT).show();
            return;
        }

        setActiveChip(chipAll);
        tvSearchLabel.setText("🔍 Kết quả tìm kiếm: \"" + query + "\"");
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getApi().searchMovies(Constants.getApiKey(getContext()), "vi-VN", query, 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                            @NonNull Response<MovieResponse> response) {
                        showLoading(false);
                        List<Movie> results = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null) {
                            results.addAll(response.body().getResults());
                        }
                        mergeLocalAndShow(query, results);
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        mergeLocalAndShow(query, new ArrayList<>());
                    }
                });
    }

    private void mergeLocalAndShow(String query, List<Movie> tmdbResults) {
        if (getContext() == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CustomMovie> localList = AppDatabase.getInstance(requireContext()).customMovieDao().searchByTitle(query);
            List<Movie> finalList = new ArrayList<>();
            if (localList != null) {
                for (CustomMovie cm : localList) {
                    finalList.add(cm.toMovie());
                }
            }
            finalList.addAll(tmdbResults);
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (!finalList.isEmpty()) {
                    showResults(finalList);
                } else {
                    showEmpty("Không tìm thấy phim phù hợp với \"" + query + "\".");
                }
            });
        });
    }

    private void showResults(List<Movie> movies) {
        tvEmpty.setVisibility(View.GONE);
        recyclerView.setAdapter(new MovieAdapter(movies));
    }

    private void showEmpty(String message) {
        recyclerView.setAdapter(null);
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setAdapter(null);
            tvEmpty.setVisibility(View.GONE);
        }
    }
}

