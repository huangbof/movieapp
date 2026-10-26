package com.example.uneti.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho một tin nhắn trong đoạn chat giữa người dùng và Trợ lý AI Nitflex.
 */
public class ChatMessage {
    private String text;
    private boolean isUser;
    private long timestamp;
    private boolean isLoading;
    private List<Movie> movies;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
        this.movies = new ArrayList<>();
    }

    public ChatMessage(String text, boolean isUser, List<Movie> movies) {
        this.text = text;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.isLoading = false;
        this.movies = movies != null ? movies : new ArrayList<>();
    }

    public static ChatMessage createLoading() {
        ChatMessage msg = new ChatMessage("🤖 Trợ lý AI đang suy nghĩ...", false);
        msg.isLoading = true;
        return msg;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUser() {
        return isUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
