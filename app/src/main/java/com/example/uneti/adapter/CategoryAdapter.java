package com.example.uneti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.uneti.R;
import com.example.uneti.model.MovieCategory;

import java.util.List;

/**
 * Adapter hiển thị danh sách các hàng danh mục phim (Thịnh hành, Đề cử,
 * Đang chiếu, Sắp chiếu...), mỗi hàng là một RecyclerView cuộn ngang
 * (PosterAdapter) có tiêu đề riêng.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<MovieCategory> categories;

    public CategoryAdapter(List<MovieCategory> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_row, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        MovieCategory category = categories.get(position);
        holder.tvTitle.setText(category.getTitle());

        holder.recyclerViewRow.setLayoutManager(new LinearLayoutManager(
                holder.itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewRow.setAdapter(new PosterAdapter(category.getMovies()));
    }

    @Override
    public int getItemCount() {
        return categories == null ? 0 : categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        RecyclerView recyclerViewRow;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvCategoryTitle);
            recyclerViewRow = itemView.findViewById(R.id.recyclerViewRow);
        }
    }
}
