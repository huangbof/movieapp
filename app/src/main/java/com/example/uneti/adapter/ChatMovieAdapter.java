package com.example.uneti.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.uneti.DetailActivity;
import com.example.uneti.R;
import com.example.uneti.model.Movie;
import com.example.uneti.network.Constants;

import java.util.List;

/**
 * Adapter hiển thị danh sách thẻ phim nhỏ nằm ngang ngay trong tin nhắn của Trợ lý AI.
 * Người dùng bấm vào thẻ phim để chuyển tới DetailActivity.
 */
public class ChatMovieAdapter extends RecyclerView.Adapter<ChatMovieAdapter.MovieCardViewHolder> {

    private final List<Movie> movies;

    public ChatMovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public MovieCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_movie_card, parent, false);
        return new MovieCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieCardViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.tvMovieTitle.setText(movie.getTitle());
        holder.tvMovieRating.setText(String.format("★ %.1f", movie.getVoteAverage()));

        String posterPath = movie.getPosterPath();
        String posterUrl = (posterPath != null && (posterPath.startsWith("http://") || posterPath.startsWith("https://")))
                ? posterPath : (Constants.IMAGE_BASE_URL + posterPath);

        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .into(holder.imgMoviePoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, movie.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }

    static class MovieCardViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMoviePoster;
        TextView tvMovieRating;
        TextView tvMovieTitle;

        public MovieCardViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMoviePoster = itemView.findViewById(R.id.imgMoviePoster);
            tvMovieRating = itemView.findViewById(R.id.tvMovieRating);
            tvMovieTitle = itemView.findViewById(R.id.tvMovieTitle);
        }
    }
}
