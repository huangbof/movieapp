package com.example.uneti.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Quản lý đăng ký/đăng nhập đơn giản bằng SharedPreferences (lưu cục bộ trên máy).
 *
 * LƯU Ý: Đây là giải pháp cho phạm vi bài tập lớn (không có backend/server).
 * Mật khẩu lưu dạng văn bản thường, KHÔNG dùng cho sản phẩm thật.
 * Khi có server, nên thay bằng gọi API đăng ký/đăng nhập + mã hoá mật khẩu.
 */
public class SessionManager {

    private static final String PREFS_NAME = "movieapp_session";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** @return true nếu đăng ký thành công, false nếu username đã tồn tại. */
    public boolean register(String username, String password) {
        JSONObject users = getUsers();
        if (users.has(username)) {
            return false;
        }
        try {
            users.put(username, password);
        } catch (JSONException e) {
            return false;
        }
        prefs.edit().putString(KEY_USERS, users.toString()).apply();
        return true;
    }

    /** @return true nếu đăng nhập đúng tài khoản/mật khẩu. */
    public boolean login(String username, String password) {
        JSONObject users = getUsers();
        if (users.has(username)) {
            String savedPassword = users.optString(username, null);
            if (savedPassword != null && savedPassword.equals(password)) {
                prefs.edit().putString(KEY_LOGGED_IN_USER, username).apply();
                return true;
            }
        }
        return false;
    }

    public void logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply();
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public String getCurrentUser() {
        return prefs.getString(KEY_LOGGED_IN_USER, null);
    }

    private JSONObject getUsers() {
        String json = prefs.getString(KEY_USERS, "{}");
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
