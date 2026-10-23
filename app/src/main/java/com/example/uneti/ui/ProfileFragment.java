package com.example.uneti.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.uneti.LoginActivity;
import com.example.uneti.R;
import com.example.uneti.auth.SessionManager;

/** Màn hình Cá nhân: hiển thị tên tài khoản đang đăng nhập và nút Đăng xuất. */
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
        tvUsername.setText(username != null ? username : "Khách");

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            sessionManager.logout();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
}
