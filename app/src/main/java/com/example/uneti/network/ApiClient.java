package com.example.uneti.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton tạo và cung cấp instance của Retrofit/TmdbApi
 * để các Fragment/Activity gọi API mà không cần khởi tạo lại.
 */
public class ApiClient {

    private static Retrofit retrofit;

    public static TmdbApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(TmdbApi.class);
    }
}
