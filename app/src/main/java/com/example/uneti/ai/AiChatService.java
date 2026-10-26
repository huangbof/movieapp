package com.example.uneti.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.uneti.auth.SessionManager;
import com.example.uneti.data.AppDatabase;
import com.example.uneti.data.CustomMovie;
import com.example.uneti.model.ChatMessage;
import com.example.uneti.model.Movie;
import com.example.uneti.model.MovieResponse;
import com.example.uneti.network.ApiClient;
import com.example.uneti.network.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

/**
 * Service xử lý phản hồi thông minh cho Chatbot AI Nitflex.
 * - Hỗ trợ gọi trực tiếp Google Gemini API (nếu được cài đặt API Key trong Cài đặt Admin).
 * - Tích hợp Bộ não AI phim ảnh cục bộ (Local Movie Intelligence Engine):
 *   + Tự động nhận diện ý định & thể loại (Hành động, Kinh dị, Hài, Tình cảm, Viễn tưởng, Điểm cao...).
 *   + Tích hợp trực tiếp dữ liệu phim TMDB và Phim độc quyền Nitflex (Room DB).
 *   + Đính kèm thẻ phim tương tác (người dùng bấm vào thẻ phim để mở ngay màn hình Chi tiết / Trailer).
 */
public class AiChatService {

    private static final String TAG = "AiChatService";
    private final Context context;
    private final SessionManager sessionManager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ChatCallback {
        void onSuccess(ChatMessage reply);
        void onError(String error);
    }

    public AiChatService(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(context);
    }

    public void sendMessage(String userMessage, ChatCallback callback) {
        executor.execute(() -> {
            String geminiKey = sessionManager.getGeminiApiKey();

            // Nếu có Gemini API Key, thử gọi Gemini API trước
            if (geminiKey != null && !geminiKey.trim().isEmpty()) {
                String geminiResponse = callGeminiApi(geminiKey.trim(), userMessage);
                if (geminiResponse != null && !geminiResponse.trim().isEmpty()) {
                    // Tìm kiếm thêm 2-3 phim liên quan để đính kèm thẻ
                    List<Movie> matchedMovies = findRelatedMovies(userMessage);
                    postResult(callback, new ChatMessage(geminiResponse, false, matchedMovies));
                    return;
                }
            }

            // Mặc định hoặc Fallback: Chạy Bộ phân tích Nitflex Movie AI Engine
            ChatMessage localReply = processLocalMovieAi(userMessage);
            postResult(callback, localReply);
        });
    }

    /**
     * Gọi Google Gemini REST API (gemini-1.5-flash)
     */
    private String callGeminiApi(String apiKey, String prompt) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setConnectTimeout(9000);
            conn.setReadTimeout(12000);
            conn.setDoOutput(true);

            JSONObject contentObj = new JSONObject();
            JSONArray partsArray = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", "Bạn là Trợ lý AI Nitflex - trợ lý tư vấn phim chuyên nghiệp, nhiệt tình và thân thiện của ứng dụng xem phim Nitflex. " +
                    "Hãy trả lời người dùng bằng tiếng Việt tự nhiên, có emoji sinh động, gợi ý phim phù hợp và tóm tắt ngắn gọn lý do nên xem.\nCâu hỏi: " + prompt);
            partsArray.put(part);
            contentObj.put("parts", partsArray);

            JSONArray contents = new JSONArray();
            contents.put(contentObj);

            JSONObject body = new JSONObject();
            body.put("contents", contents);

            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input, 0, input.length);
            }

            int code = conn.getResponseCode();
            if (code == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject resJson = new JSONObject(sb.toString());
                JSONArray candidates = resJson.optJSONArray("candidates");
                if (candidates != null && candidates.length() > 0) {
                    JSONObject firstCandidate = candidates.getJSONObject(0);
                    JSONObject content = firstCandidate.getJSONObject("content");
                    JSONArray resParts = content.getJSONArray("parts");
                    if (resParts.length() > 0) {
                        return resParts.getJSONObject(0).getString("text");
                    }
                }
            } else {
                Log.w(TAG, "Gemini API returned error code: " + code);
            }
        } catch (Exception e) {
            Log.e(TAG, "Gemini API call failed, falling back to local AI engine: " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * Bộ não AI Phim Ảnh Nitflex: Phân tích ngữ nghĩa tiếng Việt, truy vấn TMDB & CSDL Room
     */
    private ChatMessage processLocalMovieAi(String input) {
        String lower = input.toLowerCase().trim();
        String apiKey = Constants.getApiKey(context);

        // 1. Phim độc quyền Nitflex (Custom Movie trong Room)
        if (lower.contains("độc quyền") || lower.contains("nitflex") || lower.contains("admin") || lower.contains("tự tạo")) {
            List<CustomMovie> customs = AppDatabase.getInstance(context).customMovieDao().getAll();
            if (customs != null && !customs.isEmpty()) {
                List<Movie> movies = new ArrayList<>();
                for (CustomMovie cm : customs) {
                    movies.add(new Movie(cm.getId(), cm.getTitle(), cm.getPosterPath(), cm.getBackdropPath(), cm.getOverview(), cm.getReleaseDate(), cm.getVoteAverage()));
                }
                return new ChatMessage("⭐ Đây là các bộ phim Độc Quyền Nitflex do ban quản trị phát hành đặc biệt! Bấm vào từng poster để xem nội dung và thưởng thức ngay nhé: 🍿🎬", false, movies);
            } else {
                return new ChatMessage("⭐ Hiện tại kho phim Độc Quyền Nitflex đang chờ Admin cập nhật thêm siêu phẩm mới. Bạn hãy khám phá các bom tấn đang thịnh hành bên dưới nhé!", false, getPopularMoviesList(apiKey));
            }
        }

        // 2. Chào hỏi / Thông tin về Bot
        if (lower.matches(".*(xin chào|chào|hello|hi|hey|alo|bạn là ai|giới thiệu).*") && !lower.contains("phim")) {
            return new ChatMessage("Xin chào bạn! 🍿 Tôi là Trợ lý AI Nitflex.\n\nTôi có thể giúp bạn:\n" +
                    "• Gợi ý phim theo thể loại (Hành động, Kinh dị, Hài, Lãng mạn...)\n" +
                    "• Đề xuất phim hot, phim điểm cao nhất thế giới\n" +
                    "• Tìm kiếm phim theo tựa đề hoặc tâm trạng hôm nay\n" +
                    "• Khám phá các phim Độc quyền Nitflex\n\nBạn đang có tâm trạng muốn xem thể loại phim gì?", false);
        }

        // 3. Phim hành động (Action - TMDB genre 28)
        if (lower.contains("hành động") || lower.contains("action") || lower.contains("bắn súng") || lower.contains("võ thuật") || lower.contains("siêu anh hùng")) {
            List<Movie> list = getDiscoverMovies(apiKey, "28");
            return new ChatMessage("💥 Dưới đây là những siêu phẩm Hành Động mãn nhãn, kịch tính nghẹt thở nhất hiện nay dành cho bạn! Bấm vào phim để xem chi tiết & trailer:", false, list);
        }

        // 4. Phim kinh dị (Horror - TMDB genre 27)
        if (lower.contains("kinh dị") || lower.contains("horror") || lower.contains("ma") || lower.contains("rùng rợn") || lower.contains("sợ hãi") || lower.contains("giật gân")) {
            List<Movie> list = getDiscoverMovies(apiKey, "27");
            return new ChatMessage("👻 Chuẩn bị tinh thần giật mình và lạnh gáy nhé! Đây là danh sách những bộ phim kinh dị rùng rợn nhất được đánh giá cao trên Nitflex:", false, list);
        }

        // 5. Phim tình cảm / lãng mạn (Romance - TMDB genre 10749)
        if (lower.contains("tình cảm") || lower.contains("lãng mạn") || lower.contains("romance") || lower.contains("yêu") || lower.contains("hẹn hò") || lower.contains("ngọt ngào")) {
            List<Movie> list = getDiscoverMovies(apiKey, "10749");
            return new ChatMessage("💖 Những câu chuyện tình ngọt ngào, sâu lắng và đong đầy cảm xúc rất thích hợp để thư giãn tối nay:", false, list);
        }

        // 6. Phim hài hước (Comedy - TMDB genre 35)
        if (lower.contains("hài") || lower.contains("hài hước") || lower.contains("comedy") || lower.contains("cười") || lower.contains("xả stress") || lower.contains("vui vẻ")) {
            List<Movie> list = getDiscoverMovies(apiKey, "35");
            return new ChatMessage("😂 Cần xả stress sau một ngày dài? Những bộ phim hài hước duyên dáng này chắc chắn sẽ mang lại nhiều tiếng cười cho bạn:", false, list);
        }

        // 7. Hoạt hình / Anime (Animation - TMDB genre 16)
        if (lower.contains("hoạt hình") || lower.contains("anime") || lower.contains("animation") || lower.contains("disney") || lower.contains("ghibli")) {
            List<Movie> list = getDiscoverMovies(apiKey, "16");
            return new ChatMessage("🎨 Thế giới hoạt hình rực rỡ sắc màu, giàu ý nghĩa và cảm xúc dành cho bạn đây:", false, list);
        }

        // 8. Khoa học viễn tưởng (Sci-Fi - TMDB genre 878)
        if (lower.contains("viễn tưởng") || lower.contains("sci-fi") || lower.contains("vũ trụ") || lower.contains("tương lai") || lower.contains("khoa học")) {
            List<Movie> list = getDiscoverMovies(apiKey, "878");
            return new ChatMessage("🚀 Đưa bạn du hành xuyên không gian và khám phá công nghệ tương lai qua những kiệt tác viễn tưởng này:", false, list);
        }

        // 9. Điểm cao nhất / Oscar / Hay nhất
        if (lower.contains("điểm cao") || lower.contains("hay nhất") || lower.contains("top") || lower.contains("oscar") || lower.contains("imdb") || lower.contains("kinh điển")) {
            List<Movie> list = getTopRatedList(apiKey);
            return new ChatMessage("🏆 Bảng vàng những kiệt tác điện ảnh có điểm đánh giá cao nhất mọi thời đại từ khán giả toàn cầu:", false, list);
        }

        // 10. Tìm kiếm theo tên phim cụ thể (Ví dụ: "Phim Avatar", "Oppenheimer", "Spider Man")
        String queryTerm = extractMovieQuery(input);
        if (!queryTerm.isEmpty()) {
            List<Movie> searchResult = searchMoviesList(apiKey, queryTerm);
            if (!searchResult.isEmpty()) {
                Movie top = searchResult.get(0);
                String desc = (top.getOverview() != null && !top.getOverview().isEmpty())
                        ? ("\n\n📖 Tóm tắt: " + top.getOverview()) : "";
                return new ChatMessage("🔍 Nitflex AI đã tìm thấy thông tin bộ phim \"" + top.getTitle() + "\" (Đánh giá: ★ " + String.format("%.1f", top.getVoteAverage()) + ")!" + desc + "\n\nBạn có thể bấm vào thẻ phim bên dưới để xem trailer ngay nhé:", false, searchResult);
            }
        }

        // 11. Mặc định: Gợi ý các phim Hot đang thịnh hành
        List<Movie> hotList = getPopularMoviesList(apiKey);
        return new ChatMessage("🍿 Nitflex AI gợi ý cho bạn danh sách những bộ phim đang thịnh hành và được xem nhiều nhất tuần này. Bạn có thể hỏi tôi chi tiết hơn về bất kỳ thể loại nào bạn thích nhé!", false, hotList);
    }

    private String extractMovieQuery(String input) {
        String clean = input.replaceAll("(?i)(tìm phim|gợi ý phim|cho tôi xem phim|xem phim|phim|review phim|thông tin phim)", "").trim();
        return clean.length() >= 2 ? clean : "";
    }

    private List<Movie> getDiscoverMovies(String apiKey, String genreId) {
        try {
            Response<MovieResponse> res = ApiClient.getApi().discoverMovies(apiKey, "vi-VN", genreId, "popularity.desc", 1).execute();
            if (res.isSuccessful() && res.body() != null && res.body().getResults() != null) {
                return limitMovies(res.body().getResults(), 6);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error discoverMovies: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Movie> getPopularMoviesList(String apiKey) {
        try {
            Response<MovieResponse> res = ApiClient.getApi().getPopularMovies(apiKey, "vi-VN", 1).execute();
            if (res.isSuccessful() && res.body() != null && res.body().getResults() != null) {
                return limitMovies(res.body().getResults(), 6);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getPopular: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Movie> getTopRatedList(String apiKey) {
        try {
            Response<MovieResponse> res = ApiClient.getApi().getTopRatedMovies(apiKey, "vi-VN", 1).execute();
            if (res.isSuccessful() && res.body() != null && res.body().getResults() != null) {
                return limitMovies(res.body().getResults(), 6);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getTopRated: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Movie> searchMoviesList(String apiKey, String query) {
        try {
            Response<MovieResponse> res = ApiClient.getApi().searchMovies(apiKey, "vi-VN", query, 1).execute();
            if (res.isSuccessful() && res.body() != null && res.body().getResults() != null && !res.body().getResults().isEmpty()) {
                return limitMovies(res.body().getResults(), 4);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searchMovies: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<Movie> findRelatedMovies(String userMessage) {
        String apiKey = Constants.getApiKey(context);
        String query = extractMovieQuery(userMessage);
        if (!query.isEmpty()) {
            return searchMoviesList(apiKey, query);
        }
        return Collections.emptyList();
    }

    private List<Movie> limitMovies(List<Movie> list, int limit) {
        if (list == null) return Collections.emptyList();
        if (list.size() <= limit) return list;
        return new ArrayList<>(list.subList(0, limit));
    }

    private void postResult(ChatCallback callback, ChatMessage message) {
        mainHandler.post(() -> callback.onSuccess(message));
    }
}
