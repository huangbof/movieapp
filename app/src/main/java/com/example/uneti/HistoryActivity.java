package com.example.uneti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.adapter.HistoryAdapter;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.HistoryDao;
import com.example.uneti.data.HistoryMovie;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private HistoryDao historyDao;
    private HistoryAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyDao = AppDatabase.getInstance(this).historyDao();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HistoryAdapter(new HistoryAdapter.OnHistoryClickListener() {
            @Override
            public void onItemClick(HistoryMovie movie) {
                Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, movie.getMovieId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(HistoryMovie movie) {
                executor.execute(() -> {
                    historyDao.delete(movie.getMovieId());
                    runOnUiThread(() -> {
                        Toast.makeText(HistoryActivity.this, "Đã xóa khỏi lịch sử", Toast.LENGTH_SHORT).show();
                        loadHistory();
                    });
                });
            }
        });
        rvHistory.setAdapter(adapter);

        findViewById(R.id.btnClearAll).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử xem phim?")
                    .setPositiveButton("Xóa", (dialog, which) -> {
                        executor.execute(() -> {
                            historyDao.clearAll();
                            runOnUiThread(() -> {
                                Toast.makeText(HistoryActivity.this, "Đã xóa toàn bộ lịch sử", Toast.LENGTH_SHORT).show();
                                loadHistory();
                            });
                        });
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<HistoryMovie> list = historyDao.getAll();
            runOnUiThread(() -> {
                adapter.setData(list);
                if (list.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
            });
        });
    }
}
