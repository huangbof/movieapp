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
import com.example.uneti.data.CustomMovie;
import com.example.uneti.network.Constants;

import java.util.List;

public class AdminMovieAdapter extends RecyclerView.Adapter<AdminMovieAdapter.MovieViewHolder> {

    public interface OnMovieActionListener {
        void onEdit(CustomMovie movie);
        void onDelete(CustomMovie movie);
    }

    private final List<CustomMovie> movieList;
    private final OnMovieActionListener listener;

    public AdminMovieAdapter(List<CustomMovie> movieList, OnMovieActionListener listener) {
        this.movieList = movieList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        CustomMovie movie = movieList.get(position);

        holder.tvMovieTitle.setText(movie.getTitle());
        String meta = "★ " + String.format("%.1f", movie.getVoteAverage())
                + "  ·  " + (movie.getCategory() != null ? movie.getCategory() : "Phim nội bộ")
                + (movie.getReleaseDate() != null && !movie.getReleaseDate().isEmpty() ? " (" + movie.getReleaseDate() + ")" : "");
        holder.tvMovieMeta.setText(meta);

        if (movie.getVideoKey() != null && !movie.getVideoKey().isEmpty()) {
            holder.tvTrailerBadge.setText("▶ YouTube: " + movie.getVideoKey());
            holder.tvTrailerBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvTrailerBadge.setVisibility(View.GONE);
        }

        String posterPath = movie.getPosterPath();
        String posterUrl = (posterPath != null && (posterPath.startsWith("http://") || posterPath.startsWith("https://")))
                ? posterPath : (Constants.IMAGE_BASE_URL + posterPath);

        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .into(holder.imgMovieThumb);

        holder.btnEditMovie.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(movie);
        });

        holder.btnDeleteMovie.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(movie);
        });
    }

    @Override
    public int getItemCount() {
        return movieList == null ? 0 : movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMovieThumb;
        TextView tvMovieTitle;
        TextView tvMovieMeta;
        TextView tvTrailerBadge;
        TextView btnEditMovie;
        TextView btnDeleteMovie;

        MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMovieThumb = itemView.findViewById(R.id.imgMovieThumb);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvMovieMeta = itemView.findViewById(R.id.tvMovieMeta);
            tvTrailerBadge = itemView.findViewById(R.id.tvTrailerBadge);
            btnEditMovie = itemView.findViewById(R.id.btnEditMovie);
            btnDeleteMovie = itemView.findViewById(R.id.btnDeleteMovie);
        }
    }
}
