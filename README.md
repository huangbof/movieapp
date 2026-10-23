# 🎬 App Xem Phim

Ứng dụng xem phim trên Android (Java), giao diện lấy cảm hứng từ Netflix / FPT
Play (nền đen, đỏ nhấn), dữ liệu phim lấy từ **TMDB API**, có **Chatbox AI gợi
ý phim** dùng **Claude API**.

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/language-Java-007396?logo=java&logoColor=white)
![Min SDK](https://img.shields.io/badge/minSdk-24-blue)
![License](https://img.shields.io/badge/license-Educational-lightgrey)

> Đồ án môn **Thực hành lập trình và phát triển ứng dụng thiết bị di động** —
> Trường Đại học Kinh tế - Kỹ thuật Công nghiệp (UNETI), Khoa Công nghệ
> Thông tin.

---

## Mục lục

1. [Tính năng](#tính-năng)
2. [Công nghệ sử dụng](#công-nghệ-sử-dụng)
3. [Cài đặt API Key (TMDB + Claude)](#cài-đặt-api-key-tmdb--claude)
4. [Chạy project](#chạy-project)
5. [Cấu trúc project](#cấu-trúc-project)
6. [Giới hạn cần lưu ý](#giới-hạn-cần-lưu-ý)
7. [Hướng phát triển tiếp theo](#hướng-phát-triển-tiếp-theo)
8. [Tác giả](#tác-giả)

---

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

## Cài đặt API Key (TMDB + Claude)

### 1. TMDB — bắt buộc, dùng cho toàn bộ dữ liệu phim

1. Tạo tài khoản tại https://www.themoviedb.org/
2. Vào **Settings → API → Create** → chọn loại **Developer**
3. Copy **API Key (v3 auth)**
4. Mở `app/src/main/java/com/uneti/movieapp/network/Constants.java`, thay:
   ```java
   public static final String API_KEY = "YOUR_TMDB_API_KEY";
   ```

### 2. Claude API — bắt buộc nếu muốn dùng Chatbox AI

1. Tạo tài khoản tại https://console.anthropic.com/
2. Vào **API Keys** → tạo key mới → copy lại
3. Vẫn trong `Constants.java`, thay:
   ```java
   public static final String CLAUDE_API_KEY = "YOUR_CLAUDE_API_KEY";
   ```
4. (Tuỳ chọn) Xem model mới nhất tại https://docs.claude.com để đổi
   `CLAUDE_MODEL` nếu cần (mặc định `"claude-sonnet-5"`).

> ⚠️ **Lưu ý bảo mật**: đây là app demo cho bài tập lớn nên API Key được nhúng
> thẳng trong app cho đơn giản. **Không làm vậy với sản phẩm thật** — ai cũng
> có thể giải nén file APK để lấy lại key. Sản phẩm thật cần một server trung
> gian giữ key và gọi API hộ app.

## Chạy project

1. Clone repo này về máy:
   ```bash
   git clone https://github.com/huangbof/movieapp.git
   ```
2. Mở Android Studio → **Open** → chọn thư mục vừa clone.
3. Thêm API Key như hướng dẫn ở trên.
4. Đợi **Gradle Sync** hoàn tất rồi nhấn **Run ▶**.
5. Màn hình đầu tiên là Đăng nhập — bấm "Chưa có tài khoản? Đăng ký ngay" để
   tạo tài khoản demo, sau đó đăng nhập lại.

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

Bảng màu chủ đạo (`res/values/colors.xml`): nền `#0A0A0A` / `#141414`, đỏ nhấn
`#E50914` — lấy cảm hứng từ Netflix/FPT Play.

## Giới hạn cần lưu ý

- **Tài khoản lưu cục bộ, chưa có server thật** — phù hợp phạm vi bài tập lớn.
  Nếu cần backend thật, nên thay `SessionManager` bằng API đăng ký/đăng nhập
  có mã hoá mật khẩu.
- **Chỉ xem trailer qua YouTube**, không phát phim đầy đủ (theo quyết định ban
  đầu về bản quyền nội dung).
- Nếu TMDB không có trailer cho một phim, nút "Xem trailer" sẽ báo rõ thay vì
  im lặng.

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
