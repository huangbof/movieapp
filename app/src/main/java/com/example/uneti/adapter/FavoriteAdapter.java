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
import com.example.uneti.data.FavoriteMovie;
import com.example.uneti.network.Constants;

import java.util.List;

/** Adapter hiển thị danh sách phim Yêu thích đã lưu trong Room. */
public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final List<FavoriteMovie> favorites;

    public FavoriteAdapter(List<FavoriteMovie> favorites) {
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteMovie movie = favorites.get(position);

        holder.tvTitle.setText(movie.getTitle());
        holder.tvRating.setText(String.format("★ %.1f", movie.getVoteAverage()));

        String posterPath = movie.getPosterPath();
        String posterUrl = (posterPath != null && (posterPath.startsWith("http://") || posterPath.startsWith("https://")))
                ? posterPath : (Constants.IMAGE_BASE_URL + posterPath);

        Glide.with(holder.itemView.getContext())
                .load(posterUrl)
                .placeholder(R.drawable.ic_launcher)
                .error(R.drawable.ic_launcher)
                .into(holder.imgPoster);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_MOVIE_ID, movie.getId());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favorites == null ? 0 : favorites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPoster;
        TextView tvTitle, tvRating;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPoster = itemView.findViewById(R.id.imgPoster);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}
