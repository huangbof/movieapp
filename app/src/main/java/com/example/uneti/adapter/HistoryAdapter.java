package com.example.uneti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uneti.R;
import com.example.uneti.data.HistoryMovie;
import com.example.uneti.network.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    public interface OnHistoryClickListener {
        void onItemClick(HistoryMovie movie);
        void onDeleteClick(HistoryMovie movie);
    }

    private final List<HistoryMovie> list = new ArrayList<>();
    private final OnHistoryClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public HistoryAdapter(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<HistoryMovie> newList) {
        list.clear();
        if (newList != null) {
            list.addAll(newList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryMovie item = list.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvRating.setText(String.format(Locale.getDefault(), "⭐ %.1f / 10", item.getVoteAverage()));
        holder.tvTime.setText("Đã xem: " + dateFormat.format(new Date(item.getViewedAt())));

        String posterPath = item.getPosterPath();
        String posterUrl = (posterPath != null && (posterPath.startsWith("http://") || posterPath.startsWith("https://")))
                ? posterPath : (Constants.IMAGE_BASE_URL + posterPath);
        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.color.bgCard)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvRating, tvTime, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

