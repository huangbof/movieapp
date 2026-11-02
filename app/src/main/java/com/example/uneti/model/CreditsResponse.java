package com.example.uneti.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model chứa danh sách diễn viên (cast) và đoàn làm phim (crew) trả về từ TMDB API.
 */
public class CreditsResponse {

    private int id;

    @SerializedName("cast")
    private List<Cast> cast;

    public int getId() {
        return id;
    }

    public List<Cast> getCast() {
        return cast;
    }
}
