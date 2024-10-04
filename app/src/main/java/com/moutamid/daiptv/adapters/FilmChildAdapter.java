package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fxn.stash.Stash;
import com.google.android.material.card.MaterialCardView;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.activities.DetailActivity;
import com.moutamid.daiptv.listener.ItemSelectedFilm;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class FilmChildAdapter extends RecyclerView.Adapter<FilmChildAdapter.ChildVH> {

    private final Context context;
    private final ArrayList<VodModel> list;
    private final ItemSelectedFilm itemSelected;
    private final boolean isTopRated;
    private final ScrollPosition scrollPosition;

    public interface ScrollPosition {
        void scroll(int pos);
    }

    public FilmChildAdapter(Context context, ArrayList<VodModel> list, ItemSelectedFilm itemSelected, boolean isTopRated, ScrollPosition scrollPosition) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
        this.isTopRated = isTopRated;
        this.scrollPosition = scrollPosition;
    }

    @NonNull
    @Override
    public ChildVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isTopRated ? R.layout.top_items : R.layout.film_child_item;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ChildVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildVH holder, int position) {
        VodModel model = list.get(position);  // Use `position` directly

        holder.name.setText("");
        if (isTopRated) {
            holder.count.setText(String.valueOf(position + 1));
        }

        setFilmImage(holder, model);

        holder.bannerFilms.setOnClickListener(v -> {
            Stash.put(Constants.PASS, model);
            context.startActivity(new Intent(context, DetailActivity.class));
        });

        holder.bannerFilms.setOnLongClickListener(v -> {
            addFavorite(model);
            return true;
        });

        setFocusListeners(holder, model);
    }

    private void setFilmImage(ChildVH holder, VodModel model) {
        String link = getImageLink(model.stream_icon);
        Glide.with(context)
                .load(link)
                .placeholder(R.color.transparent)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object object, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        holder.name.setVisibility(View.VISIBLE);
                        holder.image.setVisibility(View.GONE);
                        holder.name.setText(model.name);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object object, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.image);
    }

    private String getImageLink(String icon) {
        if (Pattern.compile(Constants.URL_REGEX).matcher(icon.trim()).matches()) {
            return icon.trim();
        } else {
            return Constants.getImageLink(icon.trim());
        }
    }

    private void addFavorite(VodModel model) {
        FavoriteModel favoriteModel = new FavoriteModel();
        favoriteModel.id = UUID.randomUUID().toString();
        favoriteModel.image = model.stream_icon;
        favoriteModel.name = model.name;
        favoriteModel.category_id = model.category_id;
        favoriteModel.extension = model.container_extension;
        favoriteModel.type = model.stream_type;
        favoriteModel.stream_id = model.stream_id;
        new AddFavoriteDialog(context, favoriteModel, null).show();
    }

    private void setFocusListeners(ChildVH holder, VodModel model) {
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.bannerFilms.requestFocus();
            }
        });

        holder.bannerFilms.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (isTopRated) {
                    scrollPosition.scroll(holder.getAdapterPosition());
                }
                itemSelected.selected(model);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ChildVH extends RecyclerView.ViewHolder {
        TextView count, name;
        ImageView image;
        MaterialCardView bannerFilms;

        public ChildVH(@NonNull View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.count);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
            bannerFilms = itemView.findViewById(R.id.bannerFilms);
        }
    }
}

