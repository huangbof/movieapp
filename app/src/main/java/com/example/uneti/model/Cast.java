package com.example.uneti.model;

import com.google.gson.annotations.SerializedName;

/**
 * Model đại diện cho một diễn viên trong phim lấy từ endpoint credits của TMDB.
 */
public class Cast {

    private int id;
    private String name;
    private String character;

    @SerializedName("profile_path")
    private String profilePath;

    public Cast() {
    }

    public Cast(int id, String name, String character, String profilePath) {
        this.id = id;
        this.name = name;
        this.character = character;
        this.profilePath = profilePath;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCharacter() {
        return character;
    }

    public String getProfilePath() {
        return profilePath;
    }
}
