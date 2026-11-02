package com.example.uneti.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.User;
import com.example.uneti.data.UserDao;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SessionManager {

    private static final String PREFS_NAME = "movieapp_session";
    private static final String KEY_USERS_LEGACY = "users_json";
    private static final String KEY_LOGGED_IN_USER = "logged_in_user";
    private static final String KEY_CUSTOM_API_KEY = "custom_api_key";
    private static final String KEY_GEMINI_API_KEY = "gemini_api_key";
    private static final String KEY_ANNOUNCEMENT = "system_announcement";

    private final SharedPreferences prefs;
    private final UserDao userDao;

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
        Context appContext = context.getApplicationContext();
        prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userDao = AppDatabase.getInstance(appContext).userDao();

        initDefaultAdmin();
        migrateLegacyUsersFromPrefs();
    }

    private void initDefaultAdmin() {
        try {
            if (userDao.findByUsername("admin") == null) {
                userDao.insert(new User("admin", "admin123", true, System.currentTimeMillis()));
            }
        } catch (Exception ignored) {
        }
    }

    private void migrateLegacyUsersFromPrefs() {
        String legacyJson = prefs.getString(KEY_USERS_LEGACY, null);
        if (legacyJson != null && !legacyJson.isEmpty()) {
            try {
                JSONObject json = new JSONObject(legacyJson);
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String uname = keys.next();
                    String pwd = json.optString(uname, "123456");
                    boolean isAdmin = "admin".equalsIgnoreCase(uname);
                    if (userDao.findByUsername(uname) == null) {
                        userDao.insert(new User(uname, pwd, isAdmin, System.currentTimeMillis()));
                    }
                }

                prefs.edit().remove(KEY_USERS_LEGACY).apply();
            } catch (JSONException ignored) {
            }
        }
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        String cleanUname = username.trim();
        if (userDao.findByUsername(cleanUname) != null) {
            return false;
        }
        boolean isAdmin = "admin".equalsIgnoreCase(cleanUname);
        try {
            userDao.insert(new User(cleanUname, password.trim(), isAdmin, System.currentTimeMillis()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        User user = userDao.login(username.trim(), password.trim());
        if (user != null) {
            prefs.edit().putString(KEY_LOGGED_IN_USER, user.getUsername()).apply();
            return true;
        }
        return false;
    }

    public void loginWithGoogle(String email, String displayName) {
        String username = (displayName != null && !displayName.trim().isEmpty()) ? displayName.trim() : email;
        User user = userDao.findByUsername(username);
        if (user == null) {
            try {
                userDao.insert(new User(username, "google_oauth", false, System.currentTimeMillis()));
            } catch (Exception ignored) {
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

    public boolean isAdmin() {
        String current = getCurrentUser();
        if (current == null) return false;
        if ("admin".equalsIgnoreCase(current)) return true;
        User user = userDao.findByUsername(current);
        return user != null && user.isAdmin();
    }

    public List<UserInfo> getAllUserList() {
        List<UserInfo> result = new ArrayList<>();
        List<User> list = userDao.getAllUsers();
        if (list != null) {
            for (User u : list) {
                result.add(new UserInfo(u.getUsername(), u.getPassword(), u.isAdmin()));
            }
        }
        return result;
    }

    public boolean deleteUser(String username) {
        if ("admin".equalsIgnoreCase(username)) {
            return false;
        }
        User user = userDao.findByUsername(username);
        if (user != null) {
            userDao.deleteByUsername(username);
            return true;
        }
        return false;
    }

    public boolean updateUserPassword(String username, String newPassword) {
        if (username == null || newPassword == null || newPassword.trim().isEmpty()) {
            return false;
        }
        User user = userDao.findByUsername(username);
        if (user != null) {
            userDao.updatePassword(username, newPassword.trim());
            return true;
        }
        return false;
    }

    public int getUserCount() {
        return userDao.getUserCount();
    }

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
}

