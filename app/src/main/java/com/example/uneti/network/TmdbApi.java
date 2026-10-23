package com.example.uneti.network;

import com.example.uneti.model.MovieDetail;
import com.example.uneti.model.MovieResponse;
import com.example.uneti.model.VideoResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Định nghĩa các endpoint của TMDB API mà ứng dụng sử dụng.
 * Xem đầy đủ tài liệu API tại: https://developer.themoviedb.org/reference
 */
public interface TmdbApi {

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("page") int page
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("api_key") String apiKey,
            @Query("language") String language,
            @Query("query") String query,
            @Query("page") int page
    );

    @GET("movie/{id}")
    Call<MovieDetail> getMovieDetail(
            @Path("id") int movieId,
            @Query("api_key") String apiKey,
            @Query("language") String language
    );

    @GET("movie/{id}/videos")
    Call<VideoResponse> getMovieVideos(
            @Path("id") int movieId,
            @Query("api_key") String apiKey
    );
}
