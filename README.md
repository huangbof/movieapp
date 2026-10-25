# 🎬 App Xem Phim

Ứng dụng xem phim trên Android (Java), giao diện lấy cảm hứng từ Netflix / FPT

## Tính năng

| Màn hình | Mô tả |
|---|---|
| 🔐 Đăng nhập / Đăng ký | Tạo tài khoản & đăng nhập, lưu cục bộ (SharedPreferences) |
| 🏠 Trang chủ | Banner phim nổi bật + các hàng phim cuộn ngang theo danh mục (Thịnh hành, Đề cử, Đang chiếu, Sắp chiếu) |
| 🔍 Tìm kiếm | Tìm phim theo tên qua endpoint `search/movie` |
| 🎞️ Chi tiết phim | Poster/backdrop, mô tả, thể loại, xem trailer (mở YouTube), thêm/xoá Yêu thích |
| 🤖 Trợ lý AI (Chatbox) | Hỏi đáp / gợi ý phim bằng **Claude API** — chỉ hiện khi đã đăng nhập |
| ❤️ Yêu thích | Danh sách phim đã lưu, đọc/ghi bằng Room (SQLite) |
| 👤 Cá nhân | Xem tài khoản đang đăng nhập, đăng xuất |

## Công nghệ sử dụng

- **Ngôn ngữ:** Java
- **Kiến trúc UI:** 1 Activity khởi đầu (Login) + MainActivity chứa
  `BottomNavigationView` hoán đổi Fragment
- **Mạng:** Retrofit2 + Gson (gọi [TMDB API](https://www.themoviedb.org/documentation/api)
  và [Claude API](https://docs.claude.com))
- **Ảnh:** Glide
- **Lưu trữ cục bộ:** Room (SQLite) cho Yêu thích, SharedPreferences cho tài khoản
- **Build:** Gradle (Groovy DSL), AGP 8.1.4

## Cấu trúc project

```
app/src/main/java/com/uneti/movieapp/
 ├─ LoginActivity.java      // Đăng nhập / Đăng ký (gộp 1 Activity)
 ├─ MainActivity.java       // BottomNavigationView, điều hướng giữa các Fragment
 ├─ DetailActivity.java     // Chi tiết phim + xem trailer + yêu thích
 ├─ auth/
 │   └─ SessionManager.java    // Đăng nhập/đăng ký cục bộ qua SharedPreferences
 ├─ data/                      // Room (SQLite) — danh sách Yêu thích
 │   ├─ FavoriteMovie.java
 │   ├─ FavoriteDao.java
 │   └─ AppDatabase.java
 ├─ model/
 │   ├─ Movie.java, MovieResponse.java, MovieCategory.java
 │   ├─ MovieDetail.java, Genre.java          // Chi tiết phim
 │   ├─ Video.java, VideoResponse.java        // Trailer (YouTube)
 │   └─ ChatMessage.java, ChatRequestMessage.java,
 │       AnthropicRequest/Response/ContentBlock/ErrorResponse.java  // Chatbox AI
 ├─ network/
 │   ├─ Constants.java        // API_KEY, BASE_URL, CLAUDE_API_KEY...
 │   ├─ TmdbApi.java / ApiClient.java         // Retrofit cho TMDB
 │   └─ AnthropicApi.java / AnthropicClient.java  // Retrofit cho Claude API
 ├─ adapter/
 │   ├─ PosterAdapter.java, CategoryAdapter.java   // Trang chủ
 │   ├─ MovieAdapter.java, FavoriteAdapter.java     // Tìm kiếm / Yêu thích
 │   └─ ChatAdapter.java                            // Bong bóng chat
 └─ ui/
     ├─ HomeFragment.java, SearchFragment.java
     ├─ ChatFragment.java       // yêu cầu đăng nhập
     ├─ FavoriteFragment.java, ProfileFragment.java
```

## Hướng phát triển tiếp theo

- [ ] Lịch sử xem phim (bảng Room mới, log mỗi lần mở Chi tiết phim)
- [ ] Lọc phim theo thể loại (genre) ở Trang chủ / Tìm kiếm
- [ ] Trình phát video nội bộ (ExoPlayer) nếu có nguồn phim hợp lệ
- [ ] Thay xác thực cục bộ bằng backend thật

## Tác giả

Đồ án thực hiện bởi sinh viên lớp DHMT17A2HN, Khoa Công nghệ Thông tin,
Trường Đại học Kinh tế - Kỹ thuật Công nghiệp:

- Đoàn Minh Hoàng
- Điêu Việt Dũng

Giảng viên hướng dẫn: Trần Thị Huệ
