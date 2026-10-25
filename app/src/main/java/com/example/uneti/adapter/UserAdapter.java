package com.example.uneti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.auth.SessionManager;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public interface OnUserActionListener {
        void onDelete(SessionManager.UserInfo user);
        void onResetPassword(SessionManager.UserInfo user);
    }

    private final List<SessionManager.UserInfo> userList;
    private final OnUserActionListener listener;

    public UserAdapter(List<SessionManager.UserInfo> userList, OnUserActionListener listener) {
        this.userList = userList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        SessionManager.UserInfo user = userList.get(position);

        holder.tvUsername.setText(user.username);
        holder.tvPasswordHint.setText("Mật khẩu: " + user.password);

        if (user.isAdmin) {
            holder.tvRoleBadge.setVisibility(View.VISIBLE);
            holder.btnDeleteUser.setVisibility(View.GONE); // Không cho xóa Admin
        } else {
            holder.tvRoleBadge.setVisibility(View.GONE);
            holder.btnDeleteUser.setVisibility(View.VISIBLE);
        }

        holder.btnDeleteUser.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(user);
        });

        holder.btnResetPassword.setOnClickListener(v -> {
            if (listener != null) listener.onResetPassword(user);
        });
    }

    @Override
    public int getItemCount() {
        return userList == null ? 0 : userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        TextView tvRoleBadge;
        TextView tvPasswordHint;
        TextView btnResetPassword;
        TextView btnDeleteUser;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvRoleBadge = itemView.findViewById(R.id.tvRoleBadge);
            tvPasswordHint = itemView.findViewById(R.id.tvPasswordHint);
            btnResetPassword = itemView.findViewById(R.id.btnResetPassword);
            btnDeleteUser = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
