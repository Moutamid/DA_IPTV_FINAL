package com.moutamid.daiptv.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.daiptv.R;
import com.moutamid.daiptv.listener.ItemSelectedHome;
import com.moutamid.daiptv.models.TopItems;

import java.util.ArrayList;

public class HomeParentAdapter extends RecyclerView.Adapter<HomeParentAdapter.ItemVH> {

    private final Context context;
    private final ArrayList<TopItems> list;
    private final ItemSelectedHome itemSelected;
    private static final String TAG = "HomeParentAdapter";
    private final LayoutInflater inflater;

    public HomeParentAdapter(Context context, ArrayList<TopItems> list, ItemSelectedHome itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemVH(inflater.inflate(R.layout.parent_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVH holder, int position) {
        TopItems model = list.get(position);
        holder.bind(model, itemSelected);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ItemVH extends RecyclerView.ViewHolder{
        private final TextView name;
        private final RecyclerView childRC;

        public ItemVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            childRC = itemView.findViewById(R.id.childRC);

            childRC.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            childRC.setHasFixedSize(false);
        }

        public void bind(TopItems model, ItemSelectedHome itemSelected) {
            name.setText(model.name);

            HomeChildAdapter adapter = new HomeChildAdapter(itemView.getContext(), model.list, itemSelected,
                    model.name.equals("Favoris"), model.name.equals("Reprendre la lecture"),
                    childRC::smoothScrollToPosition);

            childRC.setAdapter(adapter);
        }
    }
}

