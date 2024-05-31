package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EPGModel;
import com.moutamid.daiptv.models.FavoriteModel;
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

    @Override
    public void onBindViewHolder(@NonNull ChannelVH holder, int position) {
        ChannelsModel model = list.get(holder.getAdapterPosition());
        Glide.with(context)
                .load(model.stream_icon.trim()).placeholder(R.color.transparent)
                .into(holder.image);
        holder.title.setText(model.name);
        holder.epg.setText(model.epg_channel_id);
        if (model.epg != null) {
            List<EPGModel> epgList = model.epg;
            for (EPGModel e : epgList) {
                Date startDate = Constants.parseDate(e.start);
                Date endDate = Constants.parseDate(e.end);
                if (Constants.isCurrentDateInBetween(startDate, endDate)) {
                    holder.epg.setText(e.title);
                    break;
                }
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            new AddFavoriteDialog(context, favoriteModel).show();
            return false;
        });

        holder.add.setOnClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            new AddFavoriteDialog(context, favoriteModel).show();
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
