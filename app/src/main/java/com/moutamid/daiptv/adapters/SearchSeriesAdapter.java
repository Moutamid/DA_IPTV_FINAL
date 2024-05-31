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
import com.google.android.material.card.MaterialCardView;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.activities.DetailSeriesActivity;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.List;

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
        try {
            String link = model.cover.startsWith("/") ? Constants.getImageLink(model.cover) : model.cover.trim();
            Glide.with(context).load(link).placeholder(R.color.transparent).into(holder.image);
            holder.itemView.setOnClickListener(v -> {
                Stash.put(Constants.PASS_SERIES, model);
                context.startActivity(new Intent(context, DetailSeriesActivity.class));
            });
        } catch (Exception e){
            Log.d(TAG, "onBindViewHolder: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SearchVH extends RecyclerView.ViewHolder{
        ImageView image;
        MaterialCardView add;
        MaterialCardView play;
        public SearchVH(@NonNull View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add);
//            play = itemView.findViewById(R.id.play);
            image = itemView.findViewById(R.id.image);
        }
    }

}
