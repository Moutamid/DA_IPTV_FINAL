package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.listener.EpisodeClicked;
import com.moutamid.daiptv.models.EpisodesModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;

public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.EpisodeVH> {
    Context context;
    ArrayList<EpisodesModel> list;
    EpisodeClicked episodeClicked;
    private static final String TAG = "EpisodesAdapter";

    public EpisodesAdapter(Context context, ArrayList<EpisodesModel> list, EpisodeClicked episodeClicked) {
        this.context = context;
        this.list = list;
        this.episodeClicked = episodeClicked;
    }

    @NonNull
    @Override
    public EpisodeVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EpisodeVH(LayoutInflater.from(context).inflate(R.layout.episode_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EpisodeVH holder, int position) {
        EpisodesModel model = list.get(holder.getAdapterPosition());
        Log.d(TAG, "onBindViewHolder: " + model.image);
        Log.d(TAG, "onBindViewHolder: " + model.desc);
        Glide.with(context).load(Constants.getImageLink(model.image)).placeholder(R.color.black).into(holder.coverImage);
        holder.seasonNo.setText(model.se);
        holder.name.setText(model.name);
        holder.desc.setText(model.desc);

//        TranslateAPI tagline = new TranslateAPI(
//                Language.AUTO_DETECT,   //Source Language
//                Language.FRENCH,         //Target Language
//                model.desc);           //Query Text
//
//        tagline.setTranslateListener(new TranslateAPI.TranslateListener() {
//            @Override
//            public void onSuccess(String translatedText) {
//                holder.desc.setText(translatedText);
//            }
//
//            @Override
//            public void onFailure(String ErrorText) {
//                //Log.d(TAG, "onFailure: " + ErrorText);
//            }
//        });

        holder.itemView.setOnClickListener(v -> {
            episodeClicked.clicked(model);
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class EpisodeVH extends RecyclerView.ViewHolder{
        ImageView coverImage;
        TextView seasonNo;
        TextView name;
        TextView desc;
        public EpisodeVH(@NonNull View itemView) {
            super(itemView);
            coverImage = itemView.findViewById(R.id.coverImage);
            seasonNo = itemView.findViewById(R.id.seasonNo);
            name = itemView.findViewById(R.id.name);
            desc = itemView.findViewById(R.id.desc);
        }
    }

}
