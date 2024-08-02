package com.moutamid.daiptv.utilis;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.fragments.HomeFragment;
import com.moutamid.daiptv.listener.FavoriteListener;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class AddFavoriteDialog {
    Context context;
    FavoriteModel model;
    boolean isHome;
    FavoriteListener listener;
    private static final String TAG = "AddFavoriteDialog";

    public AddFavoriteDialog(Context context, FavoriteModel model, FavoriteListener listener) {
        this.context = context;
        this.model = model;
        this.isHome = false;
        this.listener = listener;
    }

    public AddFavoriteDialog(Context context, FavoriteModel model, boolean isHome, FavoriteListener listener) {
        this.context = context;
        this.model = model;
        this.isHome = isHome;
        this.listener = listener;
    }


    public void show() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> list = Stash.getArrayList(userModel.id, FavoriteModel.class);
        boolean check = list.stream().anyMatch(favoriteModel -> favoriteModel.stream_id == model.stream_id);

        Log.d(TAG, "show: " + model.stream_id);

        String title = check ? "Supprimer des Favoris" : "Ajouter aux Favoris";
        String messgae = check ? "Souhaitez-vous retirer cet article de votre liste de favoris?" :
                "Souhaitez-vous ajouter cet article à votre liste de favoris ? Une fois ajouté, vous pourrez facilement y accéder plus tard.";
        String btn = check ? "Retirer" : "Ajouter";
        new AlertDialog.Builder(context)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(messgae)
                .setPositiveButton(btn, (dialog, which) -> {
                    dialog.dismiss();
                    if (model != null && !check) {
                        list.add(model);
                        Log.d(TAG, "show: " + model.stream_id);
                        Log.d(TAG, "show: ADDED");
                        Stash.put(userModel.id, list);
                        Toast.makeText(context, "Ajouté à la liste des favoris", Toast.LENGTH_SHORT).show();
                    } else {
                        if (model == null)
                            Toast.makeText(context, "Je ne peux pas être ajouté à la liste pour le moment", Toast.LENGTH_SHORT).show();
                        else {
                            int index = list.stream()
                                    .filter(favoriteModel -> favoriteModel.stream_id == model.stream_id)
                                    .findFirst()
                                    .map(list::indexOf)
                                    .orElse(-1);
                            if (index != -1) {
                                Log.d(TAG, "show: REMOVED");
                                list.remove(index);
                            }
                            Stash.put(userModel.id, list);
                            if (isHome) {
                                HomeFragment.refreshFavoris();
                            }
                        }
                    }
                    if (listener != null){
                        listener.isAdded(true);
                    }
                }).setNegativeButton("Fermer", (dialog, which) -> {
                    dialog.dismiss();
                    if (listener != null){
                        listener.isAdded(false);
                    }
                })
                .show();
    }
}
