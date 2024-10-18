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
import com.moutamid.daiptv.activities.DetailSeriesActivity;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SearchSeriesAdapter extends RecyclerView.Adapter<SearchSeriesAdapter.SearchVH> {

    Context context;
    ArrayList<SeriesModel> list;
    private static final String TAG = "SearchAdapter";

    public SearchSeriesAdapter(Context context, ArrayList<SeriesModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchVH(LayoutInflater.from(context).inflate(R.layout.search_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchVH holder, int position) {
        SeriesModel model = list.get(holder.getAdapterPosition());
        String link = "";

        try {
            if (Pattern.compile(Constants.URL_REGEX).matcher(model.cover.trim()).matches()) {
                link = model.cover.trim();
            } else {
                link = Constants.getImageLink(model.cover.trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
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

        holder.itemView.setOnClickListener(v -> {
            Stash.put(Constants.PASS_SERIES, model);
            context.startActivity(new Intent(context, DetailSeriesActivity.class));
        });

        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                Log.d(TAG, "model.name: " + model.name);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SearchVH extends RecyclerView.ViewHolder {
        ImageView image;
        MaterialCardView add;
        TextView name;

        public SearchVH(@NonNull View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);
        }
    }

}
