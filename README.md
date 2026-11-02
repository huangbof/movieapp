# 🎬 Nitflex - Ứng dụng Xem Phim & Quản Trị Điện Ảnh trên Android

**Nitflex** là ứng dụng xem phim trực tuyến trên nền tảng Android (ngôn ngữ Java), được thiết kế với phong cách giao diện hiện đại, sang trọng lấy cảm hứng từ Netflix, hỗ trợ chế độ nền tối (Dark Theme) xuyên suốt toàn hệ thống cùng kiến trúc Model-View-Controller (MVC) và Room Database chuẩn Android Architecture Components.

---

## 🌟 Tính năng nổi bật

| Phân hệ / Màn hình | Mô tả chi tiết |
|:---|:---|
| 🔐 **Xác thực & Phân quyền** | • Hỗ trợ đăng ký/đăng nhập tài khoản nội bộ và xác thực một chạm qua **Google Sign-In**.<br>• Quản lý tài khoản bằng **Room Database (bảng users)** và phân quyền Quản trị viên (Admin) / Người dùng.<br>• Tài khoản quản trị mặc định: `admin` / `admin123`. |
| 🏠 **Trang chủ (Home)** | • Banner phim nổi bật phong cách điện ảnh cao cấp.<br>• Các danh mục phim cuộn ngang: Thịnh hành, Đề cử, Đang chiếu, Sắp chiếu (đồng bộ từ TMDB API).<br>• Hiển thị danh mục phim nội bộ do Quản trị viên tự thêm.<br>• Thanh thông báo hệ thống hiển thị tin tức hoặc thông điệp từ Admin. |
| 🔍 **Tìm kiếm & Phân loại** | • Tìm kiếm phim tức thì theo từ khóa qua TMDB API.<br>• Bộ lọc danh mục thể loại (Hành động, Hài kịch, Kinh dị, Hoạt hình, v.v.). |
| 🎞️ **Chi tiết phim & Trailer** | • Hiển thị poster, backdrop, điểm đánh giá, tóm tắt nội dung và danh sách diễn viên (Cast).<br>• Tích hợp trình phát trailer nội bộ chuyên nghiệp (`TrailerPlayerActivity`).<br>• Thao tác thêm/xoá khỏi danh sách Yêu thích với 1 chạm.<br>• Tự động ghi nhận vào Lịch sử xem phim ngay khi người dùng truy cập. |
| ⭐ **Đánh giá & Bình luận** | • Đánh giá điểm sao (1 - 5 sao) trực quan ngay dưới màn hình chi tiết phim.<br>• Nhập nhận xét và gửi bình luận cộng đồng, lưu trữ cục bộ qua Room DB (`movie_reviews`). |
| 🕒 **Lịch sử xem phim** | • Màn hình Lịch sử chuyên biệt (`HistoryActivity`), hiển thị danh sách phim đã xem cùng mốc thời gian.<br>• Hỗ trợ xoá từng bộ phim hoặc dọn sạch toàn bộ lịch sử (`history_movies`). |
| ❤️ **Phim Yêu thích** | • Quản lý danh mục phim yêu thích cá nhân bằng Room Database (`favorite_movies`), hỗ trợ xem ngoại tuyến. |
| 🤖 **Trợ lý AI (Gemini AI)** | • Chatbot thông minh tích hợp Google Gemini AI hỗ trợ tư vấn phim, gợi ý phim theo thể loại, tâm trạng và sở thích người dùng. |
| ⚙️ **Bảng Quản trị (Admin Panel)** | • Thêm, chỉnh sửa và xoá phim tùy chỉnh trong ứng dụng (`custom_movies`).<br>• Quản lý danh sách người dùng, đổi mật khẩu và xoá tài khoản.<br>• Tuỳ biến API Key TMDB, Gemini AI Key và cập nhật thông báo hệ thống. |
| 👤 **Hồ sơ Cá nhân (Profile)** | • Giao diện VIP với header màu gradient đỏ - đen, avatar tròn và huy hiệu phân quyền (Admin / Thành viên).<br>• 3 thẻ thống kê trực quan: Số phim trong Lịch sử, Yêu thích và Đánh giá.<br>• Lối tắt truy cập nhanh vào Lịch sử xem phim và Admin Panel. |

---

## 🛠️ Công nghệ & Thư viện sử dụng

- **Ngôn ngữ phát triển:** Java (JDK 21)
- **Kiến trúc ứng dụng:** Model - View - Controller (MVC) kết hợp Repository & DAO Pattern
- **Lưu trữ dữ liệu cục bộ (Local Database):**
  - **Room Database v4 (SQLite ORM):** Quản lý 5 bảng dữ liệu gồm `users`, `favorite_movies`, `custom_movies`, `history_movies`, `movie_reviews`.
  - **SharedPreferences (`SessionManager`):** Lưu trữ token phiên đăng nhập, API Keys và cấu hình hệ thống.
- **Mạng & API (Networking):**
  - **Retrofit 2 & OkHttp 3:** Kết nối RESTful API với [The Movie Database (TMDB)](https://www.themoviedb.org/).
  - **Google Gemini AI API:** Cung cấp dịch vụ chatbot phân tích và gợi ý phim thông minh.
- **Xác thực:** Google Play Services Auth (`GoogleSignInClient`).
- **Xử lý hình ảnh:** Glide (Tải và lưu cache poster/backdrop/avatar mượt mà).
- **Giao diện người dùng:** Material Design Components, RecyclerView, ConstraintLayout, GridLayoutManager, Dark Theme đồng nhất (`MODE_NIGHT_YES`).

---

## 📁 Cấu trúc thư mục dự án

```
app/src/main/java/com/example/uneti/
 ├── AdminActivity.java            // Bảng điều khiển Quản trị viên
 ├── DetailActivity.java           // Chi tiết phim, trailer, yêu thích, đánh giá sao
 ├── HistoryActivity.java          // Quản lý lịch sử xem phim
 ├── LoginActivity.java            // Đăng nhập, đăng ký và Google Sign-In
 ├── MainActivity.java             // Điều hướng chính với BottomNavigationView
 ├── SplashActivity.java           // Màn hình khởi động, cố định Dark Theme
 ├── TrailerPlayerActivity.java    // Trình phát video trailer phim
 │
 ├── adapter/                      // Các lớp Adapter cầu nối dữ liệu RecyclerView
 │   ├── AdminMovieAdapter.java    // Danh sách phim custom trong Admin
 │   ├── CastAdapter.java          // Danh sách diễn viên
 │   ├── CategoryAdapter.java      // Danh mục thể loại trang chủ
 │   ├── ChatAdapter.java          // Tin nhắn chatbot AI
 │   ├── ChatMovieAdapter.java     // Thẻ phim gợi ý trong khung chat
 │   ├── FavoriteAdapter.java      // Danh sách phim yêu thích
 │   ├── HistoryAdapter.java       // Danh sách lịch sử xem phim
 │   ├── MovieAdapter.java         // Danh sách phim tìm kiếm / phân loại
 │   ├── PosterAdapter.java        // Phim hiển thị theo hàng ngang trang chủ
 │   ├── ReviewAdapter.java        // Danh sách đánh giá & bình luận người dùng
 │   └── UserAdapter.java          // Quản lý danh sách người dùng cho Admin
 │
 ├── ai/                           // Dịch vụ trí tuệ nhân tạo
 │   └── AiChatService.java        // Kết nối Gemini AI API & phân tích gợi ý phim
 │
 ├── auth/                         // Quản lý xác thực
 │   └── SessionManager.java       // Phiên đăng nhập, phân quyền, cấu hình API
 │
 ├── data/                         // Room Database v4 (SQLite Cục bộ)
 │   ├── AppDatabase.java          // Singleton Database kết nối 5 Entity
 │   ├── User.java & UserDao.java                    // Quản lý tài khoản
 │   ├── FavoriteMovie.java & FavoriteDao.java        // Phim yêu thích
 │   ├── HistoryMovie.java & HistoryDao.java          // Lịch sử xem phim
 │   ├── MovieReview.java & ReviewDao.java           // Đánh giá & bình luận
 │   └── CustomMovie.java & CustomMovieDao.java      // Phim nội bộ Admin
 │
 ├── model/                        // Các lớp POJO ánh xạ dữ liệu JSON
 │   ├── Movie.java, MovieResponse.java, MovieCategory.java
 │   ├── MovieDetail.java, Genre.java, Video.java, VideoResponse.java
 │   ├── Cast.java, CreditsResponse.java
 │   └── ChatMessage.java
 │
 ├── network/                      // Cấu hình giao tiếp mạng
 │   ├── ApiClient.java            // Retrofit Client
 │   ├── Constants.java            // Định nghĩa URL gốc, API Key mặc định
 │   └── TmdbApi.java              // Interface các endpoint TMDB API
 │
 └── ui/                           // Các Fragment giao diện người dùng
     ├── HomeFragment.java         // Trang chủ
     ├── SearchFragment.java       // Tìm kiếm & lọc thể loại
     ├── AiChatFragment.java       // Khung chat trợ lý AI
     ├── FavoriteFragment.java     // Phim yêu thích
     └── ProfileFragment.java      // Cá nhân, thẻ thống kê, lối tắt Admin
```

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy

1. **Yêu cầu môi trường:**
   - Android Studio Hedgehog (2023.1.1) hoặc mới hơn.
   - JDK 17 hoặc JDK 21.
   - Thiết bị Android thật hoặc máy ảo chạy Android 8.0 (API Level 26) trở lên.

2. **Các bước thực hiện:**
   ```bash
   # Clone repository về máy
   git clone https://github.com/huangbof/movieapp.git

   # Mở thư mục dự án bằng Android Studio và đợi Gradle Sync hoàn tất
   ```

3. **Tài khoản đăng nhập kiểm thử:**
   - **Tài khoản Quản trị viên (Admin):**
     - Username: `admin`
     - Password: `admin123`
     - *(Có toàn quyền truy cập mục Admin Panel để thêm phim, đổi thông báo, quản lý user)*
   - **Tài khoản Người dùng (User):**
     - Có thể tự đăng ký tài khoản mới trực tiếp trên app hoặc đăng nhập bằng Google.

---

## 👥 Nhóm tác giả

Đồ án môn học **Lập trình và Thực hành Thiết bị Di động**  
Khoa Công nghệ Thông tin — **Trường Đại học Kinh tế - Kỹ thuật Công nghiệp (UNETI)**

- **Đoàn Minh Hoàng**
- **Điêu Việt Dũng**

*Giảng viên hướng dẫn:* **Trần Thị Huệ**
