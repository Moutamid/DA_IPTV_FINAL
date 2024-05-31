package com.moutamid.daiptv;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.button.MaterialButton;
import com.moutamid.daiptv.activities.EditProfileActivity;
import com.moutamid.daiptv.activities.ManageProfileActivity;
import com.moutamid.daiptv.activities.MyListActivity;
import com.moutamid.daiptv.databinding.ActivityMainBinding;
import com.moutamid.daiptv.fragments.ChannelsFragment;
import com.moutamid.daiptv.fragments.FilmFragment;
import com.moutamid.daiptv.fragments.HomeFragment;
import com.moutamid.daiptv.fragments.RechercheFragment;
import com.moutamid.daiptv.fragments.SeriesFragment;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.Features;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    UserModel userModel;
    HomeFragment homeFragment;
    ChannelsFragment channelsFragment;
    FilmFragment filmFragment;
    SeriesFragment seriesFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        updateAndroidSecurityProvider();


        binding.profile.setOnClickListener(this::showMenu);
        binding.ancher.setOnClickListener(this::showMenu);

        homeFragment = new HomeFragment();
        channelsFragment = new ChannelsFragment();
        filmFragment = new FilmFragment();
        seriesFragment = new SeriesFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        binding.Accueil.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Home")) {
                        Constants.checkFeature(MainActivity.this, Features.HOME);
                        binding.indicatorAccueil.setVisibility(View.VISIBLE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
                    }
                }
            }
        });

        binding.reload.setOnClickListener(v -> {
            if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Home")) {
                homeFragment.refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Channels")) {
                channelsFragment.refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Film")) {
                filmFragment.refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Series")) {
                seriesFragment.refreshList();
            }
            Toast.makeText(this, "la playlist est rafraîchissante", Toast.LENGTH_SHORT).show();
        });

        binding.Chaines.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Channels")) {
                        Constants.checkFeature(MainActivity.this, Features.CHANNELS);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.VISIBLE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, channelsFragment).commit();
                    }
                }
            }
        });

        binding.Films.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Film")) {
                        Constants.checkFeature(MainActivity.this, Features.FILMS);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.VISIBLE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, filmFragment).commit();
                    }
                }
            }
        });

        binding.series.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Series")) {
                        Constants.checkFeature(MainActivity.this, Features.SERIES);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.VISIBLE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, seriesFragment).commit();
                    }
                }
            }
        });

        binding.Recherche.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Recherche")) {
                        Constants.checkFeature(MainActivity.this, Features.RECHERCHE);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.VISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RechercheFragment()).commit();
                    }
                }
            }
        });

        binding.Accueil.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.HOME);
            binding.indicatorAccueil.setVisibility(View.VISIBLE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
        });
        binding.Chaines.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.CHANNELS);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.VISIBLE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, channelsFragment).commit();
        });
        binding.Films.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.FILMS);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.VISIBLE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, filmFragment).commit();
        });
        binding.series.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.SERIES);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.VISIBLE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, seriesFragment).commit();
        });
        binding.Recherche.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.RECHERCHE);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RechercheFragment()).commit();
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
    }

    private void showMenu(View view) {
        View customLayout = LayoutInflater.from(this).inflate(R.layout.custom_popup_menu, null);
        PopupWindow popupWindow = new PopupWindow(customLayout, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        binding.ancher.animate().rotation(270f).setDuration(400).start();

        TextView name = customLayout.findViewById(R.id.name);
        name.setText(userModel.username);

        MaterialButton edit = customLayout.findViewById(R.id.edit);
        MaterialButton list = customLayout.findViewById(R.id.list);
        MaterialButton help = customLayout.findViewById(R.id.help);
        MaterialButton manage = customLayout.findViewById(R.id.manage);

        popupWindow.setOnDismissListener(() -> binding.ancher.animate().rotation(90f).setDuration(400).start());

        edit.setOnClickListener(v -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, EditProfileActivity.class));
        });
        manage.setOnClickListener(v -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, ManageProfileActivity.class));
        });
        list.setOnClickListener(v -> {
            popupWindow.dismiss();
            startActivity(new Intent(this, MyListActivity.class));
        });
        help.setOnClickListener(v -> {
            popupWindow.dismiss();
            try {
                Uri mailtoUri = Uri.parse("mailto:example123@gmail.com" +
                        "?subject=" + Uri.encode("Help & Support") +
                        "&body=" + Uri.encode("Your Complain??"));

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, mailtoUri);
                startActivity(emailIntent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        popupWindow.showAsDropDown(view);
    }

    private void updateAndroidSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}