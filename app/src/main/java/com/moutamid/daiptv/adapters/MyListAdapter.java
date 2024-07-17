package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.moutamid.daiptv.activities.VideoPlayerActivity;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ListVH> {
    Context context;
    ArrayList<FavoriteModel> list;

    public MyListAdapter(Context context, ArrayList<FavoriteModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.channels_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        FavoriteModel model = list.get(holder.getAdapterPosition());
        String link = model.image.startsWith("/") ? Constants.getImageLink(model.image) : model.image;
        Glide.with(context).load(link).placeholder(R.color.transparent).into(holder.image);
        holder.title.setText(model.name);
        holder.epg.setText(model.type);

        holder.itemView.setOnClickListener(v -> {
            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            String url;
            if (model.type.equals(Constants.TYPE_MOVIE)) {
                url = userModel.url + "/movie/" + userModel.username + "/" + userModel.password + "/" + model.stream_id + "." + model.extension;
                VodModel vodModel = new VodModel();
                vodModel.name = model.name;
                vodModel.stream_type = model.type;
                vodModel.container_extension = model.extension;
                vodModel.category_id = String.valueOf(model.id);
                vodModel.stream_id = model.stream_id;
                Stash.put(Constants.TYPE_MOVIE, vodModel);
            } else if (model.type.equals(Constants.TYPE_SERIES)) {
                url = userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + model.stream_id + "." + model.extension;
                SeriesModel seriesModel = new SeriesModel();
                seriesModel.name = model.name;
                seriesModel.stream_type = model.type;
                seriesModel.series_id = model.series_id;
                seriesModel.extension = model.extension;
                seriesModel.category_id = String.valueOf(model.id);
                Stash.put(Constants.TYPE_SERIES, seriesModel);
            } else {
                url = userModel.url + userModel.username + "/" + userModel.password + "/" + model.stream_id;
            }
            context.startActivity(new Intent(context, VideoPlayerActivity.class)
                    .putExtra("resume", String.valueOf(model.stream_id))
                    .putExtra("url", url)
                    .putExtra("channel_id", model.epg_id)
                    .putExtra("banner", model.image)
                    .putExtra("type", model.type)
                    .putExtra("name", model.name));
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ListVH extends RecyclerView.ViewHolder{
        TextView title, epg;
        ImageView image;
        MaterialCardView add;
        public ListVH(@NonNull View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add);
            title = itemView.findViewById(R.id.title);
            epg = itemView.findViewById(R.id.epg);
            image = itemView.findViewById(R.id.image);
        }
    }

}
