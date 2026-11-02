package com.example.uneti.network;

public class Constants {

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

    public static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    public static final String IMAGE_BASE_W500 = IMAGE_BASE_URL;
}

