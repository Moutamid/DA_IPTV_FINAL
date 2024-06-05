package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.listener.ItemSelectedFilm;
import com.moutamid.daiptv.models.FilmsModel;

import java.util.ArrayList;

public class FilmParentAdapter extends RecyclerView.Adapter<FilmParentAdapter.ParentVH> {
    Context context;
    ArrayList<FilmsModel> list;
    ItemSelectedFilm itemSelected;

    public FilmParentAdapter(Context context, ArrayList<FilmsModel> list, ItemSelectedFilm itemSelected) {
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
        FilmsModel model = list.get(holder.getAdapterPosition());
        holder.name.setText(model.category_name);
        LinearLayoutManager lm = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        holder.childRC.setLayoutManager(lm);
        holder.childRC.setHasFixedSize(false);
        FilmChildAdapter adapter = new FilmChildAdapter(context, model.list, itemSelected);
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