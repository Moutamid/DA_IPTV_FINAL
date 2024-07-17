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
import com.moutamid.daiptv.activities.VideoPlayerActivity;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchVH> {

    Context context;
    ArrayList<ChannelsModel> list;
    private static final String TAG = "SearchAdapter";

    public SearchAdapter(Context context, ArrayList<ChannelsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public SearchVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchVH(LayoutInflater.from(context).inflate(R.layout.search_channel_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SearchVH holder, int position) {
        ChannelsModel model = list.get(holder.getAdapterPosition());
        try {
            Glide.with(context).load(model.stream_icon.trim()).placeholder(R.color.transparent).into(holder.image);
            holder.itemView.setOnClickListener(v -> {
                UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                String url = userModel.url + userModel.username + "/" + userModel.password + "/" + model.stream_id;
                context.startActivity(new Intent(context, VideoPlayerActivity.class)
                        .putExtra("resume", String.valueOf(model.stream_id))
                        .putExtra("url", url)
                        .putExtra("channel_id", model.epg_channel_id)
                        .putExtra("name", model.name));
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
