package com.example.uneti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uneti.auth.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Màn hình Đăng nhập / Đăng ký:
 * - Hỗ trợ đăng nhập thông thường bằng tài khoản mật khẩu
 * - Tích hợp Đăng nhập bằng tài khoản Google (Google Sign-In) chuẩn theo tài liệu hướng dẫn.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private boolean isRegisterMode = false;
    private SessionManager sessionManager;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView tvFormTitle, btnSubmit, tvToggleMode;
    private EditText etUsername, etPassword, etConfirmPassword;
    private SignInButton btnGoogle;
    private View layoutOrGoogle;

    // ActivityResultLauncher nhận kết quả trả về từ Google Sign-In
    private final ActivityResultLauncher<Intent> registerForActivityResult =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                            // Thông tin email
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            // Get email
                            String email = account.getEmail();
                            // Get name
                            String name = account.getDisplayName();
                            // Chúng ta có thể lấy ảnh của người dùng
                            // String avatar = account.getPhotoUrl().toString();

                            Toast.makeText(LoginActivity.this, "email: " + email + "\nname: " + name, Toast.LENGTH_SHORT).show();

                            // Lưu session và chuyển vào MainActivity
                            sessionManager.loginWithGoogle(email, name);
                            goToMain();

                        } catch (Exception e) {
                            Log.e(TAG, "onFailure: ", e);
                            Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Thất bại", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onStart() {
        super.onStart();
        // Kiểm tra tài khoản Google đã đăng nhập trước đó
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            Toast.makeText(this, "Đã đăng nhập tài khoản " + account.getEmail(), Toast.LENGTH_SHORT).show();
            sessionManager.loginWithGoogle(account.getEmail(), account.getDisplayName());
            goToMain();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        // Cấu hình Google Sign-In theo tài liệu
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        tvFormTitle = findViewById(R.id.tvFormTitle);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnGoogle = findViewById(R.id.sign_in_button);
        layoutOrGoogle = findViewById(R.id.layoutOrGoogle);

        updateModeUI();

        tvToggleMode.setOnClickListener(v -> {
            isRegisterMode = !isRegisterMode;
            updateModeUI();
        });

        btnSubmit.setOnClickListener(v -> handleSubmit());

        // Bắt sự kiện bấm nút đăng nhập Google
        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                registerForActivityResult.launch(signInIntent);
            }
        });
    }

    private void updateModeUI() {
        if (isRegisterMode) {
            tvFormTitle.setText("Đăng ký tài khoản");
            btnSubmit.setText("Đăng ký");
            tvToggleMode.setText("Đã có tài khoản? Đăng nhập ngay");
            etConfirmPassword.setVisibility(View.VISIBLE);
            if (layoutOrGoogle != null) layoutOrGoogle.setVisibility(View.GONE);
            if (btnGoogle != null) btnGoogle.setVisibility(View.GONE);
        } else {
            tvFormTitle.setText("Đăng nhập");
            btnSubmit.setText("Đăng nhập");
            tvToggleMode.setText("Chưa có tài khoản? Đăng ký ngay");
            etConfirmPassword.setVisibility(View.GONE);
            if (layoutOrGoogle != null) layoutOrGoogle.setVisibility(View.VISIBLE);
            if (btnGoogle != null) btnGoogle.setVisibility(View.VISIBLE);
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
