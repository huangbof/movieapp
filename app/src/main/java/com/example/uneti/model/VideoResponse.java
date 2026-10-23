package com.example.uneti.model;

import java.util.List;

/** Phản hồi của endpoint GET /movie/{id}/videos. */
public class VideoResponse {
    private int id;
    private List<Video> results;

    public int getId() { return id; }
    public List<Video> getResults() { return results; }
}
