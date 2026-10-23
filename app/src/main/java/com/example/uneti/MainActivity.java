package com.example.uneti;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.uneti.auth.SessionManager;
import com.example.uneti.ui.FavoriteFragment;
import com.example.uneti.ui.HomeFragment;
import com.example.uneti.ui.ProfileFragment;
import com.example.uneti.ui.SearchFragment;

/**
 * Activity gốc của ứng dụng (chỉ vào được sau khi đăng nhập thành công):
 * chứa thanh điều hướng dưới (BottomNavigationView) với 4 tab Trang chủ,
 * Tìm kiếm, Yêu thích, Cá nhân. Mỗi tab tương ứng với một Fragment được
 * hoán đổi trong fragmentContainer.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bảo vệ: nếu chưa đăng nhập mà vào thẳng MainActivity thì đưa về LoginActivity.
        if (!new SessionManager(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                openFragment(new HomeFragment());
                return true;

            } else if (id == R.id.nav_search) {
                openFragment(new SearchFragment());
                return true;

            } else if (id == R.id.nav_favorite) {
                openFragment(new FavoriteFragment());
                return true;

            } else if (id == R.id.nav_profile) {
                openFragment(new ProfileFragment());
                return true;
            }
            return false;
        });
    }

    private void openFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
