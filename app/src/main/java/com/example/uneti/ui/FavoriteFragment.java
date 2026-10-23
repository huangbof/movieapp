package com.example.uneti.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.adapter.FavoriteAdapter;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.FavoriteDao;
import com.example.uneti.data.FavoriteMovie;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Màn hình danh sách phim Yêu thích, đọc từ cơ sở dữ liệu Room cục bộ.
 * Danh sách được tải lại mỗi khi quay về tab này (onResume) để cập nhật
 * ngay sau khi người dùng thêm/xoá yêu thích ở màn hình Chi tiết phim.
 */
public class FavoriteFragment extends Fragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private FavoriteDao favoriteDao;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewFavorites);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        favoriteDao = AppDatabase.getInstance(requireContext()).favoriteDao();
        loadFavorites();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (favoriteDao != null) {
            loadFavorites();
        }
    }

    private void loadFavorites() {
        executor.execute(() -> {
            List<FavoriteMovie> favorites = favoriteDao.getAll();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> {
                if (favorites.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setAdapter(null);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setAdapter(new FavoriteAdapter(favorites));
                }
            });
        });
    }
}
