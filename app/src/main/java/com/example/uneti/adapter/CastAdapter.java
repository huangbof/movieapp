package com.example.uneti.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.uneti.R;
import com.example.uneti.model.Cast;
import com.example.uneti.network.Constants;

import java.util.List;

public class CastAdapter extends RecyclerView.Adapter<CastAdapter.CastViewHolder> {

    private final List<Cast> castList;

    public CastAdapter(List<Cast> castList) {
        this.castList = castList;
    }

    @NonNull
    @Override
    public CastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cast, parent, false);
        return new CastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CastViewHolder holder, int position) {
        Cast cast = castList.get(position);
        holder.tvCastName.setText(cast.getName());

        String character = cast.getCharacter();
        if (character != null && !character.isEmpty()) {
            holder.tvCastCharacter.setText(character);
            holder.tvCastCharacter.setVisibility(View.VISIBLE);
        } else {
            holder.tvCastCharacter.setVisibility(View.GONE);
        }

        String profilePath = cast.getProfilePath();
        if (profilePath != null && !profilePath.isEmpty()) {
            holder.tvCastInitial.setVisibility(View.GONE);
            holder.imgCastPhoto.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView.getContext())
                    .load(Constants.IMAGE_BASE_URL + profilePath)
                    .transform(new CircleCrop())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(holder.imgCastPhoto);
        } else {

            holder.imgCastPhoto.setVisibility(View.GONE);
            holder.tvCastInitial.setVisibility(View.VISIBLE);
            String name = cast.getName();
            holder.tvCastInitial.setText(name != null && !name.isEmpty()
                    ? String.valueOf(name.charAt(0)).toUpperCase() : "?");
        }
    }

    @Override
    public int getItemCount() {
        return castList != null ? castList.size() : 0;
    }

    static class CastViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCastPhoto;
        TextView tvCastName, tvCastCharacter, tvCastInitial;

        public CastViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCastPhoto = itemView.findViewById(R.id.imgCastPhoto);
            tvCastName = itemView.findViewById(R.id.tvCastName);
            tvCastCharacter = itemView.findViewById(R.id.tvCastCharacter);
            tvCastInitial = itemView.findViewById(R.id.tvCastInitial);
        }
    }
}

