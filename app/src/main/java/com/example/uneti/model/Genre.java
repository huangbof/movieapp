package com.example.uneti.model;

/** Thể loại phim (ví dụ: Hành động, Hài, Kinh dị...). */
public class Genre {
    private int id;
    private String name;

    public Genre() {
    }

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}
