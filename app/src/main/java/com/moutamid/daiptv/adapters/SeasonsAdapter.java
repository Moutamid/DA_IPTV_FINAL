package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.daiptv.R;
import com.moutamid.daiptv.listener.SeasonClicked;
import com.moutamid.daiptv.models.SeasonsItem;

import java.util.ArrayList;

public class SeasonsAdapter extends RecyclerView.Adapter<SeasonsAdapter.SeasonsVH> {
    Context context;
    ArrayList<SeasonsItem> list;
    SeasonClicked seasonClicked;

    public SeasonsAdapter(Context context, ArrayList<SeasonsItem> list, SeasonClicked seasonClicked) {
        this.context = context;
        this.list = list;
        this.seasonClicked = seasonClicked;
    }

    @NonNull
    @Override
    public SeasonsVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SeasonsVH(LayoutInflater.from(context).inflate(R.layout.seasons_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SeasonsVH holder, int position) {
        SeasonsItem seasonsItem = list.get(holder.getAdapterPosition());
        String name = "";
        if (seasonsItem.name.contains(":")) {
            name = seasonsItem.name.split(":")[0].trim();
        } else {
            name = seasonsItem.name;
        }
        holder.seasonNo.setText(name);
        holder.episodeNo.setText(seasonsItem.episode_count + " Episodes");
        holder.itemView.setOnClickListener(v -> seasonClicked.clicked(seasonsItem.season_number, Integer.parseInt(seasonsItem.episode_count)));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class SeasonsVH extends RecyclerView.ViewHolder{
        TextView seasonNo, episodeNo;
        public SeasonsVH(@NonNull View itemView) {
            super(itemView);
            seasonNo = itemView.findViewById(R.id.seasonNo);
            episodeNo = itemView.findViewById(R.id.episodeNo);
        }
    }

}
