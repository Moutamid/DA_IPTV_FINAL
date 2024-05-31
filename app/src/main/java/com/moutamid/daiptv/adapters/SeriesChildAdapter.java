package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
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

    public SeriesChildAdapter(Context context, ArrayList<SeriesModel> list, ItemSelectedSeries itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public ChildVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "onBindViewHolder: " + model.cover);
                itemSelected.selected(model);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.cover;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            new AddFavoriteDialog(context, favoriteModel).show();
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
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

        public ChildVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }

}
