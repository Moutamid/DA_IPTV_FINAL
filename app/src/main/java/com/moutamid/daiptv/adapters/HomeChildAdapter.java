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
import com.moutamid.daiptv.activities.DetailSeriesActivity;
import com.moutamid.daiptv.activities.VideoPlayerActivity;
import com.moutamid.daiptv.listener.ItemSelectedHome;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
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
    boolean favoris;
    boolean reprendreLaLecture;

    public HomeChildAdapter(Context context, ArrayList<MovieModel> list, ItemSelectedHome itemSelected, boolean favoris, boolean reprendreLaLecture) {
        this.context = context;
        this.list = list;
        this.reprendreLaLecture = reprendreLaLecture;
        this.favoris = favoris;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public MovieVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (reprendreLaLecture) {
            return new MovieVH(LayoutInflater.from(context).inflate(R.layout.resume_home, parent, false));
        } else if (!favoris) {
            return new MovieVH(LayoutInflater.from(context).inflate(R.layout.top_items_home, parent, false));
        }
        return new MovieVH(LayoutInflater.from(context).inflate(R.layout.child_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVH holder, int position) {
        MovieModel model = list.get(holder.getAdapterPosition());

        if (!favoris && !reprendreLaLecture){
            holder.count.setText(String.valueOf(holder.getAdapterPosition()+1));
        }

        String link;
        if (reprendreLaLecture) {
            holder.name.setText(model.original_title);
            link = Constants.getImageLink(model.banner);
            Log.d(TAG, "onBindViewHolder: " + link);
        } else {
            link = model.type.equals(Constants.TYPE_SERIES) && favoris ? model.banner : Constants.getImageLink(model.banner);
        }
        Glide.with(context).load(link).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object object, @NonNull Target<Drawable> target, boolean isFirstResource) {
                holder.name.setVisibility(View.VISIBLE);
                holder.image.setVisibility(View.GONE);
                holder.name.setText(model.original_title);
                return false;
            }

            @Override
            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).placeholder(R.color.grey2).into(holder.image);

        holder.banner.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.banner;
            favoriteModel.name = model.original_title;
            favoriteModel.category_id = String.valueOf(model.id);
            favoriteModel.type = model.type;
            favoriteModel.steam_id = model.streamID;
            new AddFavoriteDialog(context, favoriteModel, true).show();
            return true;
        });

        if (reprendreLaLecture){
            holder.banner.setOnClickListener(v -> {
                UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                String url;
                if (model.type.equals(Constants.TYPE_MOVIE)) {
                    url = userModel.url + "/movie/" + userModel.username + "/" + userModel.password + "/" + model.streamID + "." + model.extension;
                    VodModel vodModel = new VodModel();
                    vodModel.name = model.original_title;
                    vodModel.stream_type = model.type;
                    vodModel.container_extension = model.extension;
                    vodModel.category_id = String.valueOf(model.id);
                    vodModel.stream_id = model.streamID;
                    Stash.put(Constants.TYPE_MOVIE, vodModel);
                } else {
                    url = userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + model.streamID + "." + model.extension;
                    SeriesModel seriesModel = new SeriesModel();
                    seriesModel.name = model.original_title;
                    seriesModel.stream_type = model.type;
                    seriesModel.extension = model.extension;
                    seriesModel.category_id = String.valueOf(model.id);
                    Stash.put(Constants.TYPE_SERIES, seriesModel);
                }
                context.startActivity(new Intent(context, VideoPlayerActivity.class)
                        .putExtra("resume", String.valueOf(model.streamID))
                        .putExtra("url", url)
                        .putExtra("banner", model.banner)
                        .putExtra("type", model.type)
                        .putExtra("name", model.original_title));
            });
        }

        holder.banner.setOnClickListener(v -> {
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
                vodModel.stream_id = model.streamID;
                vodModel.container_extension = model.extension;
                vodModel.added = model.release_date;
                vodModel.stream_type = Constants.topRated;
                Stash.put(Constants.PASS, vodModel);
                context.startActivity(new Intent(context, DetailActivity.class));
            }
        });

        holder.banner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    Log.d(TAG, "onFocusChange: Image " + model.banner);
                    Log.d(TAG, "onFocusChange: TYPE " + model.type);
                    Log.d(TAG, "onFocusChange: NAME " + model.original_title);
                    itemSelected.selected(model);
                    if (reprendreLaLecture) {
                        holder.banner.requestFocus();
                    }
                }
            }
        });

        if (reprendreLaLecture) {
            holder.banner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Log.d(TAG, "onFocusChange: Image " + model.banner);
                        Log.d(TAG, "onFocusChange: TYPE " + model.type);
                        Log.d(TAG, "onFocusChange: NAME " + model.original_title);
                        itemSelected.selected(model);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MovieVH extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name;
        MaterialCardView banner;
        TextView count;

        public MovieVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            name = itemView.findViewById(R.id.name);
            banner = itemView.findViewById(R.id.banner);
            count = itemView.findViewById(R.id.count);
        }
    }

}
