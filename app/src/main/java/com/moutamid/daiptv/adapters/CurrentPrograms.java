package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.activities.VideoPlayerActivity;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EpgListings;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CurrentPrograms extends RecyclerView.Adapter<CurrentPrograms.ProgramVh> {

    Context context;
    List<EpgListings> list;
    ChannelsModel model;
    private static final String TAG = "CurrentPrograms";

    public CurrentPrograms(Context context, List<EpgListings> list, ChannelsModel model) {
        this.context = context;
        this.list = list;
        this.model = model;
    }

    @NonNull
    @Override
    public ProgramVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProgramVh(LayoutInflater.from(context).inflate(R.layout.current_program_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ProgramVh holder, int position) {
        EpgListings epg = list.get(holder.getAbsoluteAdapterPosition());
        byte[] decodedBytes = null;
        String decodedString;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            decodedBytes = Base64.getDecoder().decode(epg.title);
            decodedString = new String(decodedBytes);
            holder.button.setText(decodedString);
        } else {
            decodedString = "";
        }

        holder.itemView.setOnClickListener(v -> {
            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            String link = userModel.url + userModel.username + "/" + userModel.password + "/" + epg.id;
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
            context.startActivity(new Intent(context, VideoPlayerActivity.class)
                    .putExtra("url", link)
                    .putExtra("channel_id", epg.epg_id)
                    .putExtra("type", Constants.TYPE_CHANNEL)
                    .putExtra("name", decodedString));
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ProgramVh extends RecyclerView.ViewHolder {
        MaterialButton button;
        public ProgramVh(@NonNull View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.button);
        }
    }

}
