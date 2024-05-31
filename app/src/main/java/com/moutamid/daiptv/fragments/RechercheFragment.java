package com.moutamid.daiptv.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.SearchAdapter;
import com.moutamid.daiptv.adapters.SearchFilmsAdapter;
import com.moutamid.daiptv.adapters.SearchSeriesAdapter;
import com.moutamid.daiptv.databinding.FragmentRechercheBinding;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class RechercheFragment extends Fragment {
    FragmentRechercheBinding binding;
    private static final String TAG = "RechercheFragment";
    ArrayList<ChannelsModel> channels;
    ArrayList<VodModel> film;
    ArrayList<SeriesModel> series;
    SearchAdapter channelAdapter;
    SearchFilmsAdapter filmAdapter;
    SearchSeriesAdapter seriesAdapter;
    Thread thread;

    public RechercheFragment() {
    }

    Dialog dialog;

    private void initializeDialog() {
        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Recherche");
    }

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.mContext = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRechercheBinding.inflate(getLayoutInflater(), container, false);

        initializeDialog();

        channels = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
        film = Stash.getArrayList(Constants.FILMS, VodModel.class);
        series = Stash.getArrayList(Constants.SERIES, SeriesModel.class);

        channelAdapter = new SearchAdapter(mContext, new ArrayList<>());
        filmAdapter = new SearchFilmsAdapter(mContext, new ArrayList<>());
        seriesAdapter = new SearchSeriesAdapter(mContext, new ArrayList<>());

        binding.chainesRC.setAdapter(channelAdapter);
        binding.filmsRC.setAdapter(filmAdapter);
        binding.seriesRC.setAdapter(seriesAdapter);

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty() && s.toString().length() >= 4) {
                    dialog.show();
                    if (thread != null) {
                        if (thread.isAlive()) {
                            thread.interrupt();
                        }
                    }
                    thread = new Thread(() -> {
                        String name = s.toString().trim();
                        ArrayList<ChannelsModel> tempChannels = new ArrayList<>();
                        for (ChannelsModel channelsModel : channels) {
                            if (channelsModel.name.toLowerCase(Locale.getDefault()).contains(name.toLowerCase(Locale.getDefault()))) {
                                tempChannels.add(channelsModel);
                            }
                        }
                        channelAdapter = new SearchAdapter(mContext, tempChannels);

                        ArrayList<SeriesModel> tempSeries = new ArrayList<>();
                        for (SeriesModel channelsModel : series) {
                            if (channelsModel.name.toLowerCase(Locale.getDefault()).contains(name.toLowerCase(Locale.getDefault()))) {
                                tempSeries.add(channelsModel);
                            }
                        }
                        seriesAdapter = new SearchSeriesAdapter(mContext, tempSeries);

                        ArrayList<VodModel> tempFilms = new ArrayList<>();
                        for (VodModel channelsModel : film) {
                            if (channelsModel.name.toLowerCase(Locale.getDefault()).contains(name.toLowerCase(Locale.getDefault()))) {
                                tempFilms.add(channelsModel);
                            }
                        }
                        filmAdapter = new SearchFilmsAdapter(mContext, tempFilms);

                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            binding.chainesRC.setAdapter(channelAdapter);
                            binding.filmsRC.setAdapter(filmAdapter);
                            binding.seriesRC.setAdapter(seriesAdapter);
                        });
                    });
                    thread.start();
                } else {
                    channelAdapter = new SearchAdapter(mContext, new ArrayList<>());
                    filmAdapter = new SearchFilmsAdapter(mContext, new ArrayList<>());
                    seriesAdapter = new SearchSeriesAdapter(mContext, new ArrayList<>());

                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        binding.chainesRC.setAdapter(channelAdapter);
                        binding.filmsRC.setAdapter(filmAdapter);
                        binding.seriesRC.setAdapter(seriesAdapter);
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return binding.getRoot();
    }
}
