package com.moutamid.daiptv;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.fxn.stash.Stash;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.button.MaterialButton;
import com.moutamid.daiptv.activities.EditProfileActivity;
import com.moutamid.daiptv.activities.ManageProfileActivity;
import com.moutamid.daiptv.activities.MyListActivity;
import com.moutamid.daiptv.database.AppDatabase;
import com.moutamid.daiptv.databinding.ActivityMainBinding;
import com.moutamid.daiptv.fragments.ChannelsFragment;
import com.moutamid.daiptv.fragments.FilmFragment;
import com.moutamid.daiptv.fragments.HomeFragment;
import com.moutamid.daiptv.fragments.RechercheFragment;
import com.moutamid.daiptv.fragments.SeriesFragment;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.Features;
import com.moutamid.daiptv.utilis.MyAlarmReceiver;

import java.util.Calendar;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends BaseActivity {
    ActivityMainBinding binding;
    UserModel userModel;
    HomeFragment homeFragment;
   // ChannelsFragment channelsFragment;
    FilmFragment filmFragment;
    SeriesFragment seriesFragment;
    AppDatabase database;

    enum CURRENT_CHANNEL {
        HOME, CHANNELS, FILMS, SERIES, SEARCH
    }

    CURRENT_CHANNEL currentChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        database = AppDatabase.getInstance(this);

        updateAndroidSecurityProvider();

        currentChannel = CURRENT_CHANNEL.HOME;

        new Thread(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                Intent intent = new Intent(this, SplashActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "shortcut_id")
                        .setShortLabel(getString(R.string.app_name))
                        .setLongLabel(getString(R.string.app_name))
                        .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
                        .setIntent(intent)
                        .build();
                shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
            }

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
//                if (shortcutManager.isRequestPinShortcutSupported()) {
//                    ShortcutInfo pinShortcutInfo =
//                            new ShortcutInfo.Builder(this, "shortcut_id")
//                                    .setShortLabel("Da IPTV")
//                                    .setLongLabel("Da IPTV")
//                                    .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher))
//                                    .setIntent(new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
//                                    .build();
//
//                    Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo);
//                    PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE);
//                    shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender());
//                }
//            }
        }).start();

        binding.profile.setOnClickListener(this::showMenu);
        binding.ancher.setOnClickListener(this::showMenu);

        homeFragment = new HomeFragment();
       // channelsFragment = new ChannelsFragment();
        filmFragment = new FilmFragment();
        seriesFragment = new SeriesFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        binding.Accueil.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.HOME);
            binding.indicatorAccueil.setVisibility(View.VISIBLE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
            currentChannel = CURRENT_CHANNEL.HOME;
        });
        binding.Chaines.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.CHANNELS);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.VISIBLE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChannelsFragment()).commit();
            currentChannel = CURRENT_CHANNEL.CHANNELS;
        });
        binding.Films.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.FILMS);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.VISIBLE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, filmFragment).commit();
            currentChannel = CURRENT_CHANNEL.FILMS;
        });
        binding.series.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.SERIES);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.VISIBLE);
            binding.indicatorRecherche.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, seriesFragment).commit();
            currentChannel = CURRENT_CHANNEL.SERIES;
        });
        binding.Recherche.setOnClickListener(v -> {
            Constants.checkFeature(MainActivity.this, Features.RECHERCHE);
            binding.indicatorAccueil.setVisibility(View.GONE);
            binding.indicatorChaines.setVisibility(View.GONE);
            binding.indicatorFilms.setVisibility(View.GONE);
            binding.indicatorSeries.setVisibility(View.GONE);
            binding.indicatorRecherche.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RechercheFragment()).commit();
            currentChannel = CURRENT_CHANNEL.SEARCH;
        });

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MyAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, new Random().nextInt(100), intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 10);
//        calendar.set(Calendar.HOUR_OF_DAY, 1);
//        calendar.set(Calendar.MINUTE, 0);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private static final String TAG = "MainActivity";

    @Override
    protected void onResume() {
        super.onResume();
        binding.Accueil.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("onFocusChange1", "onFocusChange1: home " + hasFocus);
                if (hasFocus) {
                    if (currentChannel != CURRENT_CHANNEL.HOME) {
                        Constants.checkFeature(MainActivity.this, Features.HOME);
                        binding.indicatorAccueil.setVisibility(View.VISIBLE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, homeFragment).commit();
                        currentChannel = CURRENT_CHANNEL.HOME;
                    }
                }
            }
        });

        binding.reload.setOnClickListener(v -> {
            if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Home")) {
                homeFragment.refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Channels")) {
                new ChannelsFragment().refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Film")) {
                filmFragment.refreshList();
            } else if (Stash.getString(Constants.SELECTED_PAGE, "Home").equals("Series")) {
                seriesFragment.refreshList();
            }
            //  Toast.makeText(this, "la playlist est rafraîchissante", Toast.LENGTH_SHORT).show();
        });

        binding.Chaines.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("onFocusChange1", "onFocusChange1: channels " + hasFocus);
                Log.d("onFocusChange1", "onFocusChange1: channels " + Stash.getString(Constants.SELECTED_PAGE, "Home"));
                if (hasFocus) {
                    if (currentChannel != CURRENT_CHANNEL.CHANNELS) {
                        Constants.checkFeature(MainActivity.this, Features.CHANNELS);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.VISIBLE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        Log.d(TAG, "onFocusChange: CHANNEL");
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ChannelsFragment()).commit();
                        currentChannel = CURRENT_CHANNEL.CHANNELS;
                    }
                }
            }
        });

        binding.Films.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("onFocusChange1", "onFocusChange1: film " + hasFocus);
                if (hasFocus) {
                    if (currentChannel != CURRENT_CHANNEL.FILMS) {
                        Constants.checkFeature(MainActivity.this, Features.FILMS);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.VISIBLE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        Log.d(TAG, "onFocusChange: FILM");
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, filmFragment).commit();
                        currentChannel = CURRENT_CHANNEL.FILMS;
                    }
                }
            }
        });

        binding.series.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("onFocusChange1", "onFocusChange1: series " + hasFocus);
                if (hasFocus) {
                    if (currentChannel != CURRENT_CHANNEL.SERIES) {
                        Constants.checkFeature(MainActivity.this, Features.SERIES);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.VISIBLE);
                        binding.indicatorRecherche.setVisibility(View.GONE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, seriesFragment).commit();
                        currentChannel = CURRENT_CHANNEL.SERIES;
                    }
                }
            }
        });

        binding.Recherche.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d("onFocusChange1", "onFocusChange1: search " + hasFocus);
                if (hasFocus) {
                    if (currentChannel != CURRENT_CHANNEL.SEARCH) {
                        Constants.checkFeature(MainActivity.this, Features.RECHERCHE);
                        binding.indicatorAccueil.setVisibility(View.GONE);
                        binding.indicatorChaines.setVisibility(View.GONE);
                        binding.indicatorFilms.setVisibility(View.GONE);
                        binding.indicatorSeries.setVisibility(View.GONE);
                        binding.indicatorRecherche.setVisibility(View.VISIBLE);
                        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new RechercheFragment()).commit();
                        currentChannel = CURRENT_CHANNEL.SEARCH;
                    }
                }
            }
        });

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