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
import com.moutamid.daiptv.activities.DetailSeriesActivity;
import com.moutamid.daiptv.listener.ItemSelectedSeries;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.UUID;

public class SeriesChildAdapter extends RecyclerView.Adapter<SeriesChildAdapter.ChildVH> {

    Context context;
    ArrayList<SeriesModel> list;
    ItemSelectedSeries itemSelected;
    boolean isTopRated;

    public SeriesChildAdapter(Context context, ArrayList<SeriesModel> list, ItemSelectedSeries itemSelected, boolean isTopRated) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
        this.isTopRated = isTopRated;
    }

    @NonNull
    @Override
    public ChildVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (isTopRated)
            return new ChildVH(LayoutInflater.from(context).inflate(R.layout.top_items_series, parent, false));
        return new ChildVH(LayoutInflater.from(context).inflate(R.layout.series_child_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChildVH holder, int position) {
        SeriesModel model = list.get(holder.getAdapterPosition());
        try {
            String link = model.cover.startsWith("/") ? Constants.getImageLink(model.cover) : model.cover.trim();
            Glide.with(context).load(link).placeholder(R.color.transparent).into(holder.image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isTopRated){
            holder.count.setText(String.valueOf(holder.getAdapterPosition()+1));
        }

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.bannerSeries.requestFocus();
            }
        });

        holder.bannerSeries.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                itemSelected.selected(model);
        });

        holder.bannerSeries.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.cover;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            favoriteModel.steam_id = model.series_id;
            new AddFavoriteDialog(context, favoriteModel).show();
            return true;
        });

        holder.bannerSeries.setOnClickListener(v -> {
            Stash.put(Constants.PASS_SERIES, model);
            context.startActivity(new Intent(context, DetailSeriesActivity.class));
        });

    }

    private static final String TAG = "FilmChildAdapter";

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChildVH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView count;
        MaterialCardView bannerSeries;

        public ChildVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            count = itemView.findViewById(R.id.count);
            bannerSeries = itemView.findViewById(R.id.bannerSeries);
        }
    }

}
