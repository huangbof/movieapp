package com.example.uneti.auth;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Quản lý đăng ký/đăng nhập và phân quyền Admin bằng SharedPreferences.
 * Tự động tạo sẵn tài khoản quản trị mặc định:
 * - Username: admin
 * - Password: admin123
 */
public class SessionManager {

    private static final String PREFS_NAME = "movieapp_session";
    private static final String KEY_USERS = "users_json";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";
    private static final String KEY_CUSTOM_API_KEY = "custom_api_key";
    private static final String KEY_GEMINI_API_KEY = "gemini_api_key";
    private static final String KEY_ANNOUNCEMENT = "system_announcement";

    private final SharedPreferences prefs;

    public static class UserInfo {
        public String username;
        public String password;
        public boolean isAdmin;

        public UserInfo(String username, String password, boolean isAdmin) {
            this.username = username;
            this.password = password;
            this.isAdmin = isAdmin;
        }
    }

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        initDefaultAdmin();
    }

    private void initDefaultAdmin() {
        JSONObject users = getUsers();
        if (!users.has("admin")) {
            try {
                users.put("admin", "admin123");
                prefs.edit().putString(KEY_USERS, users.toString()).apply();
            } catch (JSONException ignored) {
            }
        }
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

    /** Đăng nhập thông qua tài khoản Google */
    public void loginWithGoogle(String email, String displayName) {
        String username = (displayName != null && !displayName.isEmpty()) ? displayName : email;
        JSONObject users = getUsers();
        if (!users.has(username)) {
            try {
                users.put(username, "google_oauth");
                prefs.edit().putString(KEY_USERS, users.toString()).apply();
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_LOGGED_IN_USER, username).apply();
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

    /** Kiểm tra tài khoản hiện tại có quyền Quản trị viên (Admin) hay không */
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getCurrentUser());
    }

    /** Lấy danh sách tất cả tài khoản trong hệ thống */
    public List<UserInfo> getAllUserList() {
        List<UserInfo> list = new ArrayList<>();
        JSONObject users = getUsers();
        Iterator<String> keys = users.keys();
        while (keys.hasNext()) {
            String uname = keys.next();
            String pwd = users.optString(uname, "");
            boolean admin = "admin".equalsIgnoreCase(uname);
            list.add(new UserInfo(uname, pwd, admin));
        }
        return list;
    }

    /** Xóa một tài khoản (không cho phép xóa tài khoản admin gốc) */
    public boolean deleteUser(String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return false;
        }
        JSONObject users = getUsers();
        if (users.has(username)) {
            users.remove(username);
            prefs.edit().putString(KEY_USERS, users.toString()).apply();
            return true;
        }
        return false;
    }

    /** Đổi mật khẩu cho một tài khoản */
    public boolean updateUserPassword(String username, String newPassword) {
        JSONObject users = getUsers();
        if (users.has(username)) {
            try {
                users.put(username, newPassword);
                prefs.edit().putString(KEY_USERS, users.toString()).apply();
                return true;
            } catch (JSONException e) {
                return false;
            }
        }
        return false;
    }

    public int getUserCount() {
        return getUsers().length();
    }

    /** Quản lý API Key TMDB */
    public String getCustomApiKey() {
        return prefs.getString(KEY_CUSTOM_API_KEY, null);
    }

    public void setCustomApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            prefs.edit().remove(KEY_CUSTOM_API_KEY).apply();
        } else {
            prefs.edit().putString(KEY_CUSTOM_API_KEY, key.trim()).apply();
        }
    }

    /** Quản lý API Key Gemini AI (tuỳ chọn) */
    public String getGeminiApiKey() {
        return prefs.getString(KEY_GEMINI_API_KEY, null);
    }

    public void setGeminiApiKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            prefs.edit().remove(KEY_GEMINI_API_KEY).apply();
        } else {
            prefs.edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply();
        }
    }

    /** Quản lý thông báo hệ thống hiển thị ở Trang chủ */
    public String getAnnouncement() {
        return prefs.getString(KEY_ANNOUNCEMENT, "");
    }

    public void setAnnouncement(String announcement) {
        if (announcement == null || announcement.trim().isEmpty()) {
            prefs.edit().remove(KEY_ANNOUNCEMENT).apply();
        } else {
            prefs.edit().putString(KEY_ANNOUNCEMENT, announcement.trim()).apply();
        }
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
