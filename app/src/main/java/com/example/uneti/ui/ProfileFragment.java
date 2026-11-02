package com.example.uneti.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.uneti.AdminActivity;
import com.example.uneti.HistoryActivity;
import com.example.uneti.LoginActivity;
import com.example.uneti.R;
import com.example.uneti.auth.SessionManager;
import com.example.uneti.data.AppDatabase;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView tvUsername, tvRoleBadge, tvEmail;
    private TextView tvCountHistory, tvCountFavorite, tvCountReview;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());

        tvUsername = view.findViewById(R.id.tvUsername);
        tvRoleBadge = view.findViewById(R.id.tvRoleBadge);
        tvEmail = view.findViewById(R.id.tvEmail);

        tvCountHistory = view.findViewById(R.id.tvCountHistory);
        tvCountFavorite = view.findViewById(R.id.tvCountFavorite);
        tvCountReview = view.findViewById(R.id.tvCountReview);

        String username = sessionManager.getCurrentUser();
        boolean isAdmin = sessionManager.isAdmin();

        if (isAdmin) {
            tvUsername.setText(username != null ? username : "Quản trị viên");
            tvRoleBadge.setText("👑 QUẢN TRỊ VIÊN VIP (ADMIN)");
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_admin);
            tvEmail.setText("admin@nitflex.uneti.vn");
        } else {
            tvUsername.setText(username != null ? username : "Khách xem phim");
            tvRoleBadge.setText("⭐ THÀNH VIÊN NITFLEX VIP");
            tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_user);
            if (username != null && username.contains("@")) {
                tvEmail.setText(username);
            } else {
                tvEmail.setText((username != null ? username : "user") + "@uneti.edu.vn");
            }
        }

        View layoutAdminSection = view.findViewById(R.id.layoutAdminSection);
        View btnAdminPanel = view.findViewById(R.id.btnAdminPanel);
        if (isAdmin) {
            if (layoutAdminSection != null) layoutAdminSection.setVisibility(View.VISIBLE);
            btnAdminPanel.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), AdminActivity.class));
            });
        } else {
            if (layoutAdminSection != null) layoutAdminSection.setVisibility(View.GONE);
        }

        View btnHistory = view.findViewById(R.id.btnHistory);
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HistoryActivity.class));
        });

        View btnStatHistory = view.findViewById(R.id.btnStatHistory);
        btnStatHistory.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), HistoryActivity.class));
        });

        View btnStatFavorite = view.findViewById(R.id.btnStatFavorite);
        btnStatFavorite.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView nav =
                        getActivity().findViewById(R.id.bottomNavigation);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.nav_favorite);
                }
            }
        });

        View btnStatReview = view.findViewById(R.id.btnStatReview);
        btnStatReview.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Bạn có thể viết đánh giá tại trang Chi tiết mỗi bộ phim!", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> handleLogout(sessionManager));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            int historyCount = db.historyDao().getAll().size();
            int favoriteCount = db.favoriteDao().getAll().size();

            int reviewCount = 0;
            try {

                reviewCount = db.historyDao().getAll().size() > 0 ? db.historyDao().getAll().size() : 0;
            } catch (Exception ignored) {
            }

            final int fHistory = historyCount;
            final int fFavorite = favoriteCount;
            final int fReview = fReviewCount(db);

            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvCountHistory.setText(String.valueOf(fHistory));
                    tvCountFavorite.setText(String.valueOf(fFavorite));
                    tvCountReview.setText(String.valueOf(fReview));
                });
            }
        });
    }

    private int fReviewCount(AppDatabase db) {
        try {

            return db.historyDao().getAll().size();
        } catch (Exception e) {
            return 0;
        }
    }

    private void handleLogout(SessionManager sessionManager) {
        sessionManager.logout();
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            GoogleSignIn.getClient(requireContext(), gso).signOut().addOnCompleteListener(task -> {
                goToLogin();
            });
        } catch (Exception e) {
            goToLogin();
        }
    }

    private void goToLogin() {
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}

