package com.moutamid.daiptv.adapters;

import android.annotation.SuppressLint;
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
import com.moutamid.daiptv.activities.VideoPlayerActivity;
import com.moutamid.daiptv.database.AppDatabase;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EPGModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChannelsAdapter extends RecyclerView.Adapter<ChannelsAdapter.ChannelVH> {
    Context context;
    ArrayList<ChannelsModel> list;

    public ChannelsAdapter(Context context, ArrayList<ChannelsModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChannelVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChannelVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.channels_card, parent, false));
    }

    private static final String TAG = "ChannelsAdapter";

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onBindViewHolder(@NonNull ChannelVH holder, int position) {
        ChannelsModel model = list.get(holder.getAdapterPosition());
        Glide.with(context)
                .load(model.stream_icon.trim()).placeholder(R.color.transparent)
                .into(holder.image);
        holder.title.setText(model.name);

        List<EPGModel> epgList = AppDatabase.getInstance(context).epgDAO().getTitle(model.epg_channel_id.trim());
        for (EPGModel e : epgList) {
            Date startDate = Constants.parseDate(e.getStart());
            Date endDate = Constants.parseDate(e.getStop());
            if (Constants.isCurrentDateInBetween(startDate, endDate)) {
                holder.epg.setText(e.getTitle());
                break;
            }
        }

        holder.itemView.setOnClickListener(v -> {
            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            String link = userModel.url + userModel.username + "/" + userModel.password + "/" + model.stream_id;
            Log.d(TAG, "onBindViewHolder: " + link);
            ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.RECENT_CHANNELS, ChannelsModel.class);
            boolean check = false;
            for (ChannelsModel recent : channelsList) {
                if (recent.stream_id == model.stream_id) {
                    check = true;
                    break;
                }
            }
            if (!check) {
                channelsList.add(model);
                Stash.put(Constants.RECENT_CHANNELS, channelsList);
            }
            context.startActivity(new Intent(context, VideoPlayerActivity.class).putExtra("url", link).putExtra("type", Constants.TYPE_CHANNEL).putExtra("name", model.name));
        });

        holder.itemView.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            favoriteModel.epg_id = model.epg_channel_id;
            favoriteModel.stream_id = model.stream_id;
            new AddFavoriteDialog(context, favoriteModel, null).show();
            return false;
        });

        holder.add.setOnClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            favoriteModel.epg_id = model.epg_channel_id;
            favoriteModel.stream_id = model.stream_id;
            new AddFavoriteDialog(context, favoriteModel, null).show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ChannelVH extends RecyclerView.ViewHolder {
        TextView title, epg;
        ImageView image;
        MaterialCardView add;

        public ChannelVH(@NonNull View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add);
            title = itemView.findViewById(R.id.title);
            epg = itemView.findViewById(R.id.epg);
            image = itemView.findViewById(R.id.image);
        }
    }

}
