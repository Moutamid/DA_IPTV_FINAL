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

    Context context;
    ArrayList<VodModel> list;
    ItemSelectedFilm itemSelected;
    boolean isTopRated;
    public FilmChildAdapter(Context context, ArrayList<VodModel> list, ItemSelectedFilm itemSelected, boolean isTopRated) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
        this.isTopRated = isTopRated;
    }

    @NonNull
    @Override
    public ChildVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isTopRated) return new ChildVH(LayoutInflater.from(context).inflate(R.layout.top_items, parent, false));
        return new ChildVH(LayoutInflater.from(context).inflate(R.layout.film_child_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChildVH holder, int position) {
        VodModel model = list.get(holder.getAdapterPosition());
        if (isTopRated){
            holder.count.setText(String.valueOf(holder.getAdapterPosition()+1));
        }

        try {

            String link;
            if (Pattern.compile(Constants.URL_REGEX).matcher(model.stream_icon.trim()).matches()) {
                link = model.stream_icon.trim();
            } else {
                link = Constants.getImageLink(model.stream_icon.trim());
            }

           // String link = model.stream_icon.startsWith("/") ? Constants.getImageLink(model.stream_icon) : model.stream_icon.trim();

            Glide.with(context).load(link).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object object, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.image.setVisibility(View.GONE);
                    holder.name.setText(model.name);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).placeholder(R.color.transparent).into(holder.image);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        holder.itemView.setOnClickListener(v -> {
//            Stash.put(Constants.PASS, model);
//            context.startActivity(new Intent(context, DetailActivity.class));
//        });

        holder.bannerFilms.setOnClickListener(v -> {
            Stash.put(Constants.PASS, model);
            context.startActivity(new Intent(context, DetailActivity.class));
        });

        holder.bannerFilms.setOnLongClickListener(v -> {

            Log.d(TAG, "onBindViewHolder: " + model.stream_id);

            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.extension = model.container_extension;
            favoriteModel.type = model.stream_type;
            favoriteModel.stream_id = model.stream_id;
            new AddFavoriteDialog(context, favoriteModel, null).show();
            return true;
        });


        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.bannerFilms.requestFocus();
                Log.d("Constants", "onBindViewHolder: " + model.added);
//                itemSelected.selected(model);
            }
        });

        holder.bannerFilms.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
//                holder.bannerFilms.requestFocus();
//                Log.d("Constants", "onBindViewHolder: " + model.added);
                itemSelected.selected(model);
            }
        });
    }

    private static final String TAG = "FilmChildAdapter";
    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChildVH extends RecyclerView.ViewHolder {
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
