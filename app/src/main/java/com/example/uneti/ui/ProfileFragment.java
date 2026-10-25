package com.example.uneti.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.uneti.AdminActivity;
import com.example.uneti.LoginActivity;
import com.example.uneti.R;
import com.example.uneti.auth.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

/** Màn hình Cá nhân: hiển thị tên tài khoản đang đăng nhập, toggle dark mode và nút Đăng xuất. */
public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager sessionManager = new SessionManager(requireContext());

        TextView tvUsername = view.findViewById(R.id.tvUsername);
        String username = sessionManager.getCurrentUser();
        if (sessionManager.isAdmin()) {
            tvUsername.setText("👑 " + (username != null ? username : "Admin") + " (Quản trị viên)");
        } else {
            tvUsername.setText(username != null ? username : "Khách");
        }

        View btnAdminPanel = view.findViewById(R.id.btnAdminPanel);
        if (sessionManager.isAdmin()) {
            btnAdminPanel.setVisibility(View.VISIBLE);
            btnAdminPanel.setOnClickListener(v -> {
                startActivity(new Intent(getActivity(), AdminActivity.class));
            });
        } else {
            btnAdminPanel.setVisibility(View.GONE);
        }

        // Dark Mode switch
        SwitchCompat switchDarkMode = view.findViewById(R.id.switchDarkMode);
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        switchDarkMode.setChecked(currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logout();
            // Đăng xuất Google theo hướng dẫn trong tài liệu: mGoogleSignInClient.signOut()
            try {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();
                GoogleSignIn.getClient(requireContext(), gso).signOut();
            } catch (Exception ignored) {
            }

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
