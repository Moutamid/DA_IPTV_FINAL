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

    Context context;
    ArrayList<TopItems> list;
    ItemSelectedHome itemSelected;
    private static final String TAG = "HomeParentAdapter";

    public HomeParentAdapter(Context context, ArrayList<TopItems> list, ItemSelectedHome itemSelected) {
        this.context = context;
        this.list = list;
        this.itemSelected = itemSelected;
    }

    @NonNull
    @Override
    public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemVH(LayoutInflater.from(context).inflate(R.layout.parent_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ItemVH holder, int position) {
        TopItems model = list.get(holder.getAdapterPosition());
        holder.name.setText(model.name);

        HomeChildAdapter adapter = new HomeChildAdapter(context, model.list, itemSelected, model.name.equals("Favoris"), model.name.equals("Reprendre la lecture"));

        holder.childRC.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.childRC.setHasFixedSize(false);

        holder.childRC.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                Log.d(TAG, "onChildViewAttachedToWindow: " + view.isFocused());
                if (view.isFocused()) {
                    holder.childRC.requestChildFocus(view, view);
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {

            }
        });

        holder.childRC.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ItemVH extends RecyclerView.ViewHolder{
        TextView name;
        RecyclerView childRC;
        public ItemVH(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            childRC = itemView.findViewById(R.id.childRC);
        }
    }

}
