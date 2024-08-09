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
import com.moutamid.daiptv.activities.DetailActivity;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class SearchFilmsAdapter extends RecyclerView.Adapter<SearchFilmsAdapter.SearchVH> {

    Context context;
    ArrayList<VodModel> list;
    private static final String TAG = "SearchAdapter";

    public SearchFilmsAdapter(Context context, ArrayList<VodModel> list) {
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
        VodModel model = list.get(holder.getAdapterPosition());
        try {
            String link;
            if (Pattern.compile(Constants.URL_REGEX).matcher(model.stream_icon.trim()).matches()) {
                link = model.stream_icon.trim();
            } else {
                link = Constants.getImageLink(model.stream_icon.trim());
            }

            Glide.with(context).load(link).placeholder(R.color.transparent).into(holder.image);

            holder.itemView.setOnClickListener(v -> {
                Stash.put(Constants.PASS, model);
                context.startActivity(new Intent(context, DetailActivity.class));
            });

            holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus){
                    Log.d(TAG, "onBindViewHolder: " + model.name);
                }
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
