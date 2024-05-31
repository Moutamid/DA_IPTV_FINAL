package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.activities.DetailActivity;
import com.moutamid.daiptv.activities.DetailSeriesActivity;
import com.moutamid.daiptv.listener.ItemSelectedHome;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.UUID;

public class HomeChildAdapter extends RecyclerView.Adapter<HomeChildAdapter.MovieVH> {
    private static final String TAG = "HomeChildAdapter";
    Context context;
    ArrayList<MovieModel> list;
    ItemSelectedHome itemSelected;

    public HomeChildAdapter(Context context, ArrayList<MovieModel> list, ItemSelectedHome itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public MovieVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MovieVH(LayoutInflater.from(context).inflate(R.layout.child_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVH holder, int position) {
        MovieModel model = list.get(holder.getAdapterPosition());
        Glide.with(context).load(Constants.getImageLink(model.banner)).placeholder(R.color.grey2).into(holder.image);
        holder.itemView.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.banner;
            favoriteModel.name = model.original_title;
            favoriteModel.category_id = String.valueOf(model.id);
            favoriteModel.type = model.type;
            new AddFavoriteDialog(context, favoriteModel).show();
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (model.type.equals(Constants.TYPE_SERIES)) {
                SeriesModel seriesModel = new SeriesModel();
                seriesModel.name = model.original_title;
                seriesModel.cover = model.banner;
                seriesModel.releaseDate = model.release_date;
                seriesModel.stream_type = Constants.topRated;
                Stash.put(Constants.PASS_SERIES, seriesModel);
                context.startActivity(new Intent(context, DetailSeriesActivity.class));
            } else {
                VodModel vodModel = new VodModel();
                vodModel.name = model.original_title;
                vodModel.stream_icon = model.banner;
                vodModel.added = model.release_date;
                vodModel.stream_type = Constants.topRated;
                Stash.put(Constants.PASS, vodModel);
                context.startActivity(new Intent(context, DetailActivity.class));
            }
        });

        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    itemSelected.selected(model);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MovieVH extends RecyclerView.ViewHolder{
        ImageView image;
        public MovieVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
        }
    }

}
