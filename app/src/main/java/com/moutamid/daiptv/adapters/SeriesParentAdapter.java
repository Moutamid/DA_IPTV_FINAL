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

    private final Context context;
    private final ArrayList<TVModel> list;
    private final ItemSelectedSeries itemSelected;
    private final LayoutInflater inflater;

    public SeriesParentAdapter(Context context, ArrayList<TVModel> list, ItemSelectedSeries itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ParentVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParentVH(inflater.inflate(R.layout.film_parent_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParentVH holder, int position) {
        TVModel model = list.get(position);
        holder.bind(model, itemSelected);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ParentVH extends RecyclerView.ViewHolder {
        private final TextView name;
        private final RecyclerView childRC;

        public ParentVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            childRC = itemView.findViewById(R.id.filmChildRC);

            childRC.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            childRC.setHasFixedSize(false);
        }

        public void bind(TVModel model, ItemSelectedSeries itemSelected) {
            name.setText(model.category_name);

            boolean isTopRated = model.category_id.equals(Constants.topRated);
            SeriesChildAdapter adapter = new SeriesChildAdapter(itemView.getContext(), model.list, itemSelected, isTopRated, childRC::smoothScrollToPosition);
            childRC.setAdapter(adapter);
        }
    }
}
