package com.example.uneti.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity Room đại diện cho tài khoản người dùng trong hệ thống Nitflex.
 */
@Entity(tableName = "users")
public class User {

    @PrimaryKey
    @NonNull
    private String username;

    private String password;
    private boolean isAdmin;
    private long createdAt;

    public User(@NonNull String username, String password, boolean isAdmin, long createdAt) {
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
        this.createdAt = createdAt;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
