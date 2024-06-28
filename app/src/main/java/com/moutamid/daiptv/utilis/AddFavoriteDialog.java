package com.moutamid.daiptv.utilis;

import android.content.Context;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.fragments.HomeFragment;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;

import java.util.ArrayList;

public class AddFavoriteDialog {
    Context context;
    FavoriteModel model;
    boolean isHome;

    public AddFavoriteDialog(Context context, FavoriteModel model) {
        this.context = context;
        this.model = model;
        this.isHome = false;
    }

    public AddFavoriteDialog(Context context, FavoriteModel model, boolean isHome) {
        this.context = context;
        this.model = model;
        this.isHome = isHome;
    }

    public void show() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> list = Stash.getArrayList(userModel.id, FavoriteModel.class);
        boolean check = list.stream().anyMatch(favoriteModel -> favoriteModel.steam_id == model.steam_id);

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
                        Stash.put(userModel.id, list);
                        Toast.makeText(context, "Ajouté à la liste des favoris", Toast.LENGTH_SHORT).show();
                    } else {
                        if (model == null)
                            Toast.makeText(context, "Je ne peux pas être ajouté à la liste pour le moment", Toast.LENGTH_SHORT).show();
                        else {
                            int index = list.stream()
                                    .filter(favoriteModel -> favoriteModel.steam_id == model.steam_id)
                                    .findFirst()
                                    .map(list::indexOf)
                                    .orElse(-1);
                            if (index != -1) {
                                list.remove(index);
                            }
                            Stash.put(userModel.id, list);
                            if (isHome) {
                                HomeFragment.refreshFavoris();
                            }
                        }
                    }
                }).setNegativeButton("Fermer", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}
