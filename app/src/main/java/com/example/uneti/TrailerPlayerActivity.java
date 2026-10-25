package com.example.uneti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Màn hình xem trực tiếp Trailer ngay trong app Nitflex bằng WebView nhúng YouTube Player.
 * Cấu hình đầy đủ User-Agent, Origin, Referer để tránh lỗi Error 152 của YouTube,
 * đồng thời có nút fallback mở qua YouTube nếu video bị hãng phim chặn nhúng.
 */
public class TrailerPlayerActivity extends AppCompatActivity {

    public static final String EXTRA_VIDEO_KEY = "extra_video_key";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";

    private WebView webViewTrailer;
    private FrameLayout customViewContainer;
    private RelativeLayout mainPlayerContainer;
    private ProgressBar playerProgressBar;

    private View customView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private String currentVideoKey;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trailer_player);

        currentVideoKey = getIntent().getStringExtra(EXTRA_VIDEO_KEY);
        String movieTitle = getIntent().getStringExtra(EXTRA_MOVIE_TITLE);

        if (currentVideoKey == null || currentVideoKey.trim().isEmpty()) {
            Toast.makeText(this, "Không có mã video để phát.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        webViewTrailer = findViewById(R.id.webViewTrailer);
        customViewContainer = findViewById(R.id.customViewContainer);
        mainPlayerContainer = findViewById(R.id.mainPlayerContainer);
        playerProgressBar = findViewById(R.id.playerProgressBar);

        TextView tvMovieTitle = findViewById(R.id.tvMovieTitle);
        if (movieTitle != null && !movieTitle.isEmpty()) {
            tvMovieTitle.setText(movieTitle);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnOpenExternal).setOnClickListener(v -> openInYouTubeApp());

        setupWebView(currentVideoKey);
    }

    private void openInYouTubeApp() {
        if (currentVideoKey == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + currentVideoKey));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở ứng dụng YouTube.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(String videoKey) {
        WebSettings settings = webViewTrailer.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);

        // Fix lỗi Error 152: YouTube chặn WebView mặc định do cờ "; wv". Cần giả lập Chrome Mobile chuẩn.
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Mobile Safari/537.36");

        // Cho phép Cookies của YouTube
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webViewTrailer, true);

        webViewTrailer.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (uri != null) {
                    String urlStr = uri.toString();
                    // Nếu người dùng bấm vào nút "Watch on YouTube" hoặc link YouTube bên trong
                    if (urlStr.contains("youtube.com/watch") || urlStr.contains("youtu.be")) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            startActivity(intent);
                            return true;
                        } catch (Exception ignored) {
                        }
                    }
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                playerProgressBar.setVisibility(View.GONE);
            }
        });

        webViewTrailer.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customView = view;
                customViewCallback = callback;
                mainPlayerContainer.setVisibility(View.GONE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
            }

            @Override
            public void onHideCustomView() {
                if (customView == null) return;
                mainPlayerContainer.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                customViewContainer.removeView(customView);
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                }
                customView = null;
                customViewCallback = null;
            }
        });

        // Sử dụng youtube-nocookie với origin hợp lệ để tránh Error 152
        String embedUrl = "https://www.youtube-nocookie.com/embed/" + videoKey
                + "?autoplay=1&playsinline=1&modestbranding=1&rel=0&fs=1&enablejsapi=1&origin=https://com.example.uneti";

        String html = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>"
                + "<style>"
                + "* { margin:0; padding:0; box-sizing:border-box; }"
                + "html, body { width:100%; height:100%; background-color:#000000; overflow:hidden; }"
                + ".wrapper { width:100%; height:100%; display:flex; align-items:center; justify-content:center; }"
                + "iframe { width:100%; height:100%; border:none; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class='wrapper'>"
                + "<iframe src='" + embedUrl + "' "
                + "referrerpolicy='strict-origin-when-cross-origin' "
                + "allow='accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen' "
                + "allowfullscreen></iframe>"
                + "</div>"
                + "</body>"
                + "</html>";

        webViewTrailer.loadDataWithBaseURL("https://com.example.uneti", html, "text/html", "UTF-8", null);
    }

    @Override
    public void onBackPressed() {
        if (customView != null) {
            if (webViewTrailer.getWebChromeClient() != null) {
                webViewTrailer.getWebChromeClient().onHideCustomView();
            }
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webViewTrailer != null) {
            webViewTrailer.onPause();
            webViewTrailer.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webViewTrailer != null) {
            webViewTrailer.onResume();
            webViewTrailer.resumeTimers();
        }
    }

    @Override
    protected void onDestroy() {
        if (webViewTrailer != null) {
            webViewTrailer.destroy();
        }
        super.onDestroy();
    }
}
