package com.moutamid.daiptv;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.activities.LoadingScreenActivity;
import com.moutamid.daiptv.activities.LoginActivity;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;

public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            if (Constants.checkInternet(SplashActivity.this)){
                UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                if (userModel!=null) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }
            } else {
                new AlertDialog.Builder(SplashActivity.this)
                        .setCancelable(false)
                        .setTitle("Pas d'Internet")
                        .setMessage("Vérifiez votre connection internet")
                        .setPositiveButton("Recommencez", (dialog, which) -> {
                            dialog.dismiss();
                            recreate();
                        }).setNegativeButton("Fermer", (dialog, which) -> {
                            dialog.dismiss();
                            finish();
                        })
                        .show();
            }
        }, 2000);

    }
}
