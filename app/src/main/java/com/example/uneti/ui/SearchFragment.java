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
import com.example.uneti.model.MovieResponse;
import com.example.uneti.network.ApiClient;
import com.example.uneti.network.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Màn hình Tìm kiếm phim theo tên, gọi endpoint search/movie của TMDB. */
public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;

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
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        view.findViewById(R.id.btnSearch).setOnClickListener(v -> doSearch());
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                doSearch();
                return true;
            }
            return false;
        });
    }

    private void doSearch() {
        String query = etSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng nhập tên phim cần tìm.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getApi().searchMovies(Constants.API_KEY, "vi-VN", query, 1)
                .enqueue(new Callback<MovieResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MovieResponse> call,
                                            @NonNull Response<MovieResponse> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getResults() != null
                                && !response.body().getResults().isEmpty()) {
                            recyclerView.setAdapter(new MovieAdapter(response.body().getResults()));
                        } else {
                            showEmpty("Không tìm thấy phim phù hợp với \"" + query + "\".");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MovieResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showEmpty("Có lỗi khi tìm kiếm. Kiểm tra kết nối mạng và thử lại.");
                    }
                });
    }

    private void showEmpty(String message) {
        recyclerView.setAdapter(null);
        tvEmpty.setText(message);
        tvEmpty.setVisibility(View.VISIBLE);
    }
}
