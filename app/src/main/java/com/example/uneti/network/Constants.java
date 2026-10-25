package com.example.uneti.network;

/**
 * Các hằng số dùng để gọi API TMDB (The Movie Database).
 *
 * CÁCH LẤY API KEY (miễn phí):
 * 1. Tạo tài khoản tại https://www.themoviedb.org/
 * 2. Vào Settings -> API -> Create -> chọn "Developer"
 * 3. Copy "API Key (v3 auth)" và dán thay cho giá trị bên dưới.
 */
public class Constants {

    // TODO: THAY GIÁ TRỊ NÀY bằng API Key TMDB thật của bạn
    public static final String API_KEY = "2f20d15c74d91fe94fa8b289974504cd";

    public static String getApiKey(android.content.Context context) {
        if (context != null) {
            String custom = new com.example.uneti.auth.SessionManager(context).getCustomApiKey();
            if (custom != null && !custom.trim().isEmpty()) {
                return custom.trim();
            }
        }
        return API_KEY;
    }

    public static final String BASE_URL = "https://api.themoviedb.org/3/";

    // Kích thước ảnh poster: w200, w300, w500, original...
    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
}
