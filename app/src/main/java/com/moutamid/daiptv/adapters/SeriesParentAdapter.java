package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.daiptv.R;
import com.moutamid.daiptv.listener.ItemSelectedFilm;
import com.moutamid.daiptv.listener.ItemSelectedSeries;
import com.moutamid.daiptv.models.FilmsModel;
import com.moutamid.daiptv.models.TVModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;

public class SeriesParentAdapter extends RecyclerView.Adapter<SeriesParentAdapter.ParentVH> {
    Context context;
    ArrayList<TVModel> list;
    ItemSelectedSeries itemSelected;

    public SeriesParentAdapter(Context context, ArrayList<TVModel> list, ItemSelectedSeries itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public ParentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParentVH(LayoutInflater.from(context).inflate(R.layout.film_parent_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParentVH holder, int position) {
        TVModel model = list.get(holder.getAdapterPosition());
        holder.name.setText(model.category_name);
        LinearLayoutManager lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.childRC.setLayoutManager(lm);
        holder.childRC.setHasFixedSize(false);
        boolean isTopRated = model.category_id.equals(Constants.topRated);
        SeriesChildAdapter adapter = new SeriesChildAdapter(context, model.list, itemSelected, isTopRated);
        holder.childRC.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ParentVH extends RecyclerView.ViewHolder {
        TextView name;
        RecyclerView childRC;

        public ParentVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            childRC = itemView.findViewById(R.id.filmChildRC);
        }
    }

}
