package com.example.uneti.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.adapter.ChatAdapter;
import com.example.uneti.ai.AiChatService;
import com.example.uneti.model.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment màn hình Chatbox AI:
 * - Trò chuyện và nhận gợi ý phim thông minh
 * - Đính kèm thẻ phim tương tác (xem chi tiết / trailer)
 * - Thanh gợi ý nhanh các chủ đề phổ biến
 */
public class AiChatFragment extends Fragment {

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private AiChatService aiChatService;

    private RecyclerView rvChatMessages;
    private EditText etInputMessage;
    private ImageView btnSendMessage;
    private TextView btnClearChat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        aiChatService = new AiChatService(requireContext());

        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etInputMessage = view.findViewById(R.id.etInputMessage);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);
        btnClearChat = view.findViewById(R.id.btnClearChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        rvChatMessages.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(messageList);
        rvChatMessages.setAdapter(chatAdapter);

        // Tin nhắn chào mừng ban đầu
        if (messageList.isEmpty()) {
            initWelcomeMessage();
        }

        // Bắt sự kiện gửi tin nhắn
        btnSendMessage.setOnClickListener(v -> handleSendMessage());
        etInputMessage.setOnEditorActionListener((v, actionId, event) -> {
            handleSendMessage();
            return true;
        });

        // Nút xoá chat
        btnClearChat.setOnClickListener(v -> {
            messageList.clear();
            initWelcomeMessage();
            chatAdapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Đã làm mới cuộc trò chuyện", Toast.LENGTH_SHORT).show();
        });

        // Thiết lập sự kiện cho các chip gợi ý nhanh
        setupSuggestionChips(view);
    }

    private void initWelcomeMessage() {
        messageList.add(new ChatMessage(
                "Xin chào bạn! 🍿 Tôi là Trợ lý AI Nitflex.\n\n" +
                "Hôm nay bạn đang muốn xem phim gì? Tôi có thể gợi ý phim theo thể loại, tâm trạng, điểm đánh giá cao hoặc các phim độc quyền của Nitflex.\n\n" +
                "Hãy bấm vào các gợi ý bên dưới hoặc nhập câu hỏi để bắt đầu nhé! ✨",
                false
        ));
    }

    private void setupSuggestionChips(View view) {
        view.findViewById(R.id.chipToday).setOnClickListener(v -> sendQuickPrompt("Hôm nay có phim gì hay, gợi ý cho tôi"));
        view.findViewById(R.id.chipAction).setOnClickListener(v -> sendQuickPrompt("Gợi ý cho tôi những bộ phim hành động đỉnh cao nhất"));
        view.findViewById(R.id.chipHorror).setOnClickListener(v -> sendQuickPrompt("Cho tôi danh sách phim kinh dị rùng rợn, giật gân"));
        view.findViewById(R.id.chipRomance).setOnClickListener(v -> sendQuickPrompt("Gợi ý phim tình cảm lãng mạn nhẹ nhàng"));
        view.findViewById(R.id.chipComedy).setOnClickListener(v -> sendQuickPrompt("Tôi muốn xem phim hài xả stress"));
        view.findViewById(R.id.chipExclusive).setOnClickListener(v -> sendQuickPrompt("Nitflex có những bộ phim độc quyền nào?"));
        view.findViewById(R.id.chipTopRated).setOnClickListener(v -> sendQuickPrompt("Top các bộ phim có điểm số cao nhất mọi thời đại"));
    }

    private void sendQuickPrompt(String promptText) {
        etInputMessage.setText(promptText);
        handleSendMessage();
    }

    private void handleSendMessage() {
        String input = etInputMessage.getText().toString().trim();
        if (input.isEmpty()) {
            return;
        }

        etInputMessage.setText("");

        // 1. Thêm tin nhắn của User
        ChatMessage userMsg = new ChatMessage(input, true);
        messageList.add(userMsg);
        int userPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(userPos);

        // 2. Thêm bong bóng loading của AI
        ChatMessage loadingMsg = ChatMessage.createLoading();
        messageList.add(loadingMsg);
        int loadingPos = messageList.size() - 1;
        chatAdapter.notifyItemInserted(loadingPos);
        rvChatMessages.scrollToPosition(loadingPos);

        // 3. Gửi sang AI Service
        aiChatService.sendMessage(input, new AiChatService.ChatCallback() {
            @Override
            public void onSuccess(ChatMessage reply) {
                if (!isAdded()) return;

                // Xoá bong bóng loading và thêm câu trả lời thật
                int idx = messageList.indexOf(loadingMsg);
                if (idx != -1) {
                    messageList.remove(idx);
                    chatAdapter.notifyItemRemoved(idx);
                }

                messageList.add(reply);
                int replyPos = messageList.size() - 1;
                chatAdapter.notifyItemInserted(replyPos);
                rvChatMessages.smoothScrollToPosition(replyPos);
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                int idx = messageList.indexOf(loadingMsg);
                if (idx != -1) {
                    messageList.remove(idx);
                    chatAdapter.notifyItemRemoved(idx);
                }

                ChatMessage errReply = new ChatMessage("Xin lỗi, hiện tại tôi đang gặp chút sự cố kết nối. Bạn vui lòng thử lại nhé! 🍿", false);
                messageList.add(errReply);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChatMessages.smoothScrollToPosition(messageList.size() - 1);
            }
        });
    }
}
