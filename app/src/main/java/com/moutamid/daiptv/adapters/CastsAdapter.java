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
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.models.CastModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;

public class CastsAdapter extends RecyclerView.Adapter<CastsAdapter.ParentVH> {

    Context context;
    ArrayList<CastModel> list;

    public CastsAdapter(Context context, ArrayList<CastModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ParentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParentVH(LayoutInflater.from(context).inflate(R.layout.cast_card, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParentVH holder, int position) {
        CastModel model = list.get(holder.getAdapterPosition());
        holder.title.setText(model.character);
        holder.name.setText(model.name);
        Glide.with(context).load(Constants.getImageLink(model.profile_path)).into(holder.image);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ParentVH extends RecyclerView.ViewHolder{
        ImageView image;
        TextView title,name;
        public ParentVH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
        }
    }

}
