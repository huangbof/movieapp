package com.example.uneti.model;

/** Một video liên quan đến phim (trailer, teaser...) từ TMDB. */
public class Video {
    private String id;
    private String key;   // mã video trên YouTube
    private String site;  // "YouTube"
    private String type;  // "Trailer", "Teaser"...
    private String name;

    public String getId() { return id; }
    public String getKey() { return key; }
    public String getSite() { return site; }
    public String getType() { return type; }
    public String getName() { return name; }
}
