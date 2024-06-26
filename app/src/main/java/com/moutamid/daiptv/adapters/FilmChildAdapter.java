package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
            String link = model.stream_icon.startsWith("/") ? Constants.getImageLink(model.stream_icon) : model.stream_icon.trim();
            Glide.with(context).load(link).placeholder(R.color.transparent).into(holder.image);
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

        holder.itemView.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            new AddFavoriteDialog(context, favoriteModel).show();
            return true;
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.bannerFilms.requestFocus();
                Log.d("Constants", "onBindViewHolder: " + model.added);
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
        TextView count;
        ImageView image;
        MaterialCardView bannerFilms;
        public ChildVH(@NonNull View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.count);
            image = itemView.findViewById(R.id.image);
            bannerFilms = itemView.findViewById(R.id.bannerFilms);
        }
    }

}
