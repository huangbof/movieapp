package com.example.uneti;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uneti.auth.SessionManager;

/**
 * Màn hình Đăng nhập / Đăng ký (gộp chung 1 Activity, chuyển đổi bằng nút bấm
 * để tránh tạo thêm nhiều file). Tài khoản lưu cục bộ bằng SharedPreferences
 * thông qua SessionManager (xem ghi chú trong SessionManager.java).
 */
public class LoginActivity extends AppCompatActivity {

    private boolean isRegisterMode = false;

    private SessionManager sessionManager;

    private TextView tvFormTitle, btnSubmit, tvToggleMode;
    private EditText etUsername, etPassword, etConfirmPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        tvFormTitle = findViewById(R.id.tvFormTitle);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        updateModeUI();

        tvToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateModeUI();
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void updateModeUI() {
        if (isRegisterMode) {
            tvFormTitle.setText("Đăng ký tài khoản");
            btnSubmit.setText("Đăng ký");
            tvToggleMode.setText("Đã có tài khoản? Đăng nhập ngay");
            etConfirmPassword.setVisibility(View.VISIBLE);
        } else {
            tvFormTitle.setText("Đăng nhập");
            btnSubmit.setText("Đăng nhập");
            tvToggleMode.setText("Chưa có tài khoản? Đăng ký ngay");
            etConfirmPassword.setVisibility(View.GONE);
        }
    }

    private void handleSubmit() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRegisterMode) {
            String confirm = etConfirmPassword.getText().toString().trim();
            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu nhập lại không khớp.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!sessionManager.register(username, password)) {
                Toast.makeText(this, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác.", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            isRegisterMode = false;
            etPassword.setText("");
            etConfirmPassword.setText("");
            updateModeUI();

        } else {
            if (!sessionManager.login(username, password)) {
                Toast.makeText(this, "Sai tên đăng nhập hoặc mật khẩu.", Toast.LENGTH_SHORT).show();
                return;
            }
            goToMain();
        }
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
