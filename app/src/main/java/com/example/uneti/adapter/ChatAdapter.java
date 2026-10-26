package com.example.uneti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter quản lý danh sách tin nhắn giữa Người dùng và Trợ lý AI Nitflex.
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_AI = 2;

    private final List<ChatMessage> messageList;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).isUser() ? VIEW_TYPE_USER : VIEW_TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chat_ai, parent, false);
            return new AiMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        String timeStr = timeFormat.format(new Date(message.getTimestamp()));

        if (holder instanceof UserMessageViewHolder) {
            UserMessageViewHolder userHolder = (UserMessageViewHolder) holder;
            userHolder.tvUserMessage.setText(message.getText());
            userHolder.tvUserTime.setText(timeStr);

        } else if (holder instanceof AiMessageViewHolder) {
            AiMessageViewHolder aiHolder = (AiMessageViewHolder) holder;
            aiHolder.tvAiMessage.setText(message.getText());
            aiHolder.tvAiTime.setText(timeStr);

            if (message.getMovies() != null && !message.getMovies().isEmpty()) {
                aiHolder.rvRecommendedMovies.setVisibility(View.VISIBLE);
                aiHolder.rvRecommendedMovies.setLayoutManager(
                        new LinearLayoutManager(aiHolder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
                aiHolder.rvRecommendedMovies.setAdapter(new ChatMovieAdapter(message.getMovies()));
            } else {
                aiHolder.rvRecommendedMovies.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMessage, tvUserTime;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTime = itemView.findViewById(R.id.tvUserTime);
        }
    }

    static class AiMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiMessage, tvAiTime, tvAiSender;
        RecyclerView rvRecommendedMovies;

        public AiMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiSender = itemView.findViewById(R.id.tvAiSender);
            tvAiMessage = itemView.findViewById(R.id.tvAiMessage);
            tvAiTime = itemView.findViewById(R.id.tvAiTime);
            rvRecommendedMovies = itemView.findViewById(R.id.rvRecommendedMovies);
        }
    }
}
