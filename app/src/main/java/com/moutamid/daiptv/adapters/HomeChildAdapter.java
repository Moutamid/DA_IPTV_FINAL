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
import java.util.regex.Pattern;

public class HomeChildAdapter extends RecyclerView.Adapter<HomeChildAdapter.MovieVH> {
    private static final String TAG = "HomeChildAdapter";
    Context context;
    ArrayList<MovieModel> list;
    ItemSelectedHome itemSelected;
    boolean favoris;
    boolean reprendreLaLecture;

    interface ScrollPosition {
        void scroll(int pos);
    }

    ScrollPosition scrollPosition;

    public HomeChildAdapter(Context context, ArrayList<MovieModel> list, ItemSelectedHome itemSelected, boolean favoris, boolean reprendreLaLecture, ScrollPosition position) {
        this.context = context;
        this.list = list;
        this.reprendreLaLecture = reprendreLaLecture;
        this.scrollPosition = position;
        this.favoris = favoris;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public MovieVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (reprendreLaLecture) {
            return new MovieVH(LayoutInflater.from(context).inflate(R.layout.resume_home, parent, false));
        }
        String[] isRecents = list.get(0).type.split(",");
        if (isRecents.length == 2) {
            if (favoris || isRecents[1].equals(Constants.RECENTS)) {
                return new MovieVH(LayoutInflater.from(context).inflate(R.layout.child_item, parent, false));
            }
        } else {
            if (favoris) {
                return new MovieVH(LayoutInflater.from(context).inflate(R.layout.child_item, parent, false));
            }
        }
        return new MovieVH(LayoutInflater.from(context).inflate(R.layout.top_items_home, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MovieVH holder, int position) {
        MovieModel model = list.get(holder.getAbsoluteAdapterPosition());
        String[] isRecents = model.type.split(",");

        if (!favoris && !reprendreLaLecture && isRecents.length != 2) {
            holder.count.setText(String.valueOf(holder.getAbsoluteAdapterPosition() + 1));
        } else {
            holder.name.setText("");
        }

        if (reprendreLaLecture || isRecents.length != 2) {
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        Log.d(TAG, "onFocusChange: Image " + model.banner);
                        Log.d(TAG, "onFocusChange: TYPE " + model.type);
                        Log.d(TAG, "onFocusChange: NAME " + model.original_title);
                        Log.d(TAG, "onFocusChange: banner " + model.series_id);
                        itemSelected.selected(model);
                        if (reprendreLaLecture) {
                            holder.banner.requestFocus();
                        }
                    }
                }
            });
        }

        String link;
        if (Pattern.compile(Constants.URL_REGEX).matcher(model.banner).matches()) {
            link = model.banner;
        } else {
            link = Constants.getImageLink(model.banner);
        }

        if (reprendreLaLecture) {
            holder.name.setText(model.original_title);
            Log.d(TAG, "onBindViewHolder: " + link);
        }
        Glide.with(context).load(link).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object object, @NonNull Target<Drawable> target, boolean isFirstResource) {
                try {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.image.setVisibility(View.GONE);
                    holder.name.setText(model.original_title);
                } catch (Exception er) {
                    Log.d(TAG, "onLoadFailed: " + er.getLocalizedMessage());
                }
                return false;
            }

            @Override
            public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).placeholder(R.color.grey2).into(holder.image);

        holder.banner.setOnLongClickListener(v -> {
            Log.d(TAG, "onFocusChange: NAME " + model.original_title);
            Log.d(TAG, "onFocusChange: banner " + model.banner);
            Log.d(TAG, "onFocusChange: type " + model.type);
            Log.d(TAG, "onFocusChange: series_id " + model.series_id);
            Log.d(TAG, "onFocusChange: streamID " + model.streamID);
            Log.d(TAG, "onFocusChange: link " + link);

            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.banner;
            favoriteModel.name = model.original_title;
            favoriteModel.category_id = String.valueOf(model.id);
            favoriteModel.type = model.type;
            favoriteModel.stream_id = model.streamID;
            favoriteModel.series_id = model.series_id;
            favoriteModel.extension = model.extension;
            new AddFavoriteDialog(context, favoriteModel, true, null).show();
            return true;
        });

        holder.banner.setOnClickListener(v -> {
            String type = isRecents[0];
            if (type.equals(Constants.TYPE_SERIES)) {
                SeriesModel seriesModel = new SeriesModel();
                seriesModel.name = model.original_title;
                seriesModel.cover = model.banner;
                if (model.series_id == 0) {
                    seriesModel.series_id = model.streamID;
                } else {
                    seriesModel.series_id = model.series_id;
                }
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

        if (reprendreLaLecture) {
            holder.banner.setOnClickListener(v -> {
                String type = isRecents[0];
                UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                String url;
                Log.d(TAG, "onBindViewHolder: " + model.extension);
                if (type.equals(Constants.TYPE_MOVIE)) {
                    url = userModel.url + "/movie/" + userModel.username + "/" + userModel.password + "/" + model.streamID + "." + model.extension;
                    Log.d(TAG, "onBindViewHolder: URL  " + url);
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
                    seriesModel.series_id = model.series_id;
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

        holder.banner.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!favoris && !reprendreLaLecture && isRecents.length != 2) {
                        Log.d("onFocusChange123", "onFocusChange: inside IF");
                        Log.d("onFocusChange123", "onFocusChange: pos : " + holder.getAbsoluteAdapterPosition());
                        scrollPosition.scroll(holder.getAbsoluteAdapterPosition());
//                        if (holder.getAbsoluteAdapterPosition() == 0) {
//                            Log.d("onFocusChange123", "inside IF 2.0");
//                            scrollPosition.scroll(0);
//                        }
                    }
                    itemSelected.selected(model);
                }
            }
        });

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
