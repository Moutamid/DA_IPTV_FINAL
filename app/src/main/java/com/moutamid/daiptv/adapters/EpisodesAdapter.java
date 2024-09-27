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
    private final Context context;
    private final ArrayList<EpisodesModel> list;
    private final EpisodeClicked episodeClicked;
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
        EpisodesModel model = list.get(holder.getBindingAdapterPosition());
        bindData(holder, model);
        bindTranslation(holder, model.desc, model.name);
    }

    private void bindData(@NonNull EpisodeVH holder, EpisodesModel model) {
        Log.d(TAG, "Binding episode: " + model.name);

        Glide.with(context)
                .load(Constants.getImageLink(model.image))
                .placeholder(R.color.black)
                .into(holder.coverImage);

        holder.seasonNo.setText(model.se);
        holder.name.setText(model.name);
        holder.desc.setText(model.desc);

        holder.itemView.setOnClickListener(v -> episodeClicked.clicked(model));
    }

    private void bindTranslation(@NonNull EpisodeVH holder, String desc, String title) {
        if (desc != null) {
            if (!desc.isEmpty()) {
                TranslateAPI tagline = new TranslateAPI(
                        Language.AUTO_DETECT,
                        Language.FRENCH,
                        desc);

                tagline.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        holder.desc.setText(translatedText);
                        Log.d(TAG, "Translation successful: " + translatedText);
                    }

                    @Override
                    public void onFailure(String errorText) {
                        Log.e(TAG, "Translation failed: " + errorText);
                    }
                });

            }
        }

        if (title != null){
            if (!title.isEmpty()){
                TranslateAPI name = new TranslateAPI(
                        Language.AUTO_DETECT,
                        Language.FRENCH,
                        title);

                name.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        holder.name.setText(translatedText);
                        Log.d(TAG, "Translation successful: " + translatedText);
                    }

                    @Override
                    public void onFailure(String errorText) {
                        Log.e(TAG, "Translation failed: " + errorText);
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class EpisodeVH extends RecyclerView.ViewHolder {
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

