package com.moutamid.daiptv.fragments;

import static androidx.core.content.ContextCompat.getSystemService;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.SearchAdapter;
import com.moutamid.daiptv.adapters.SearchFilmsAdapter;
import com.moutamid.daiptv.adapters.SearchSeriesAdapter;
import com.moutamid.daiptv.databinding.FragmentRechercheBinding;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.FilmsModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TVModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Locale;

public class RechercheFragment extends Fragment {
    FragmentRechercheBinding binding;
    private static final String TAG = "RechercheFragment";
    ArrayList<ChannelsModel> channels;
    ArrayList<VodModel> film;
    ArrayList<FilmsModel> film_stash;
    ArrayList<TVModel> series_stash;
    ArrayList<SeriesModel> series;
    SearchAdapter channelAdapter;
    SearchFilmsAdapter filmAdapter;
    SearchSeriesAdapter seriesAdapter;

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
        film_stash = Stash.getArrayList(Constants.FILMS, FilmsModel.class);
        series_stash = Stash.getArrayList(Constants.SERIES, TVModel.class);

        film = new ArrayList<>();
        series = new ArrayList<>();

        for (FilmsModel model : film_stash) {
            film.addAll(model.list);
        }
        for (TVModel model : series_stash) {
            series.addAll(model.list);
        }

        channelAdapter = new SearchAdapter(mContext, new ArrayList<>());
        filmAdapter = new SearchFilmsAdapter(mContext, new ArrayList<>());
        seriesAdapter = new SearchSeriesAdapter(mContext, new ArrayList<>());

        binding.chainesRC.setAdapter(channelAdapter);
        binding.filmsRC.setAdapter(filmAdapter);
        binding.seriesRC.setAdapter(seriesAdapter);

        binding.searchET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // Ensure the keyboard remains open
                    if (getActivity() != null && isAdded()) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(binding.searchET, InputMethodManager.SHOW_IMPLICIT);
                    }
                }
            }
        });

        binding.searchET.addTextChangedListener(new TextWatcher() {
            private Thread thread;
            private Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text changes
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty() && s.toString().length() >= 3) {
                    dialog.show();

                    if (thread != null && thread.isAlive()) {
                        thread.interrupt();
                    }

                    // Ensure thread safety using a handler
                    handler.post(() -> {
                        thread = new Thread(() -> {
                            String name = s.toString().trim().toLowerCase(Locale.getDefault());

                            ArrayList<ChannelsModel> tempChannels = new ArrayList<>();
                            for (ChannelsModel channelsModel : channels) {
                                if (channelsModel.name.toLowerCase(Locale.getDefault()).contains(name)) {
                                    tempChannels.add(channelsModel);
                                }
                            }

                            ArrayList<SeriesModel> tempSeries = new ArrayList<>();
                            for (SeriesModel seriesModel : series) {
                                if (seriesModel.name.toLowerCase(Locale.getDefault()).contains(name)) {
                                    tempSeries.add(seriesModel);
                                }
                            }

                            ArrayList<VodModel> tempFilms = new ArrayList<>();
                            for (VodModel vodModel : film) {
                                if (vodModel.name.toLowerCase(Locale.getDefault()).contains(name)) {
                                    Log.d(TAG, "onTextChanged: MATCHED FILM " + vodModel.name);
                                    tempFilms.add(vodModel);
                                }
                            }

                            requireActivity().runOnUiThread(() -> {
                                dialog.dismiss();
                                binding.chainesRC.setAdapter(new SearchAdapter(mContext, tempChannels));
                                binding.filmsRC.setAdapter(new SearchFilmsAdapter(mContext, tempFilms));
                                binding.seriesRC.setAdapter(new SearchSeriesAdapter(mContext, tempSeries));

                                // No need to request focus here since the EditText already has it
                            });
                        });
                        thread.start();
                    });
                } else {
                    // Handle the case where the input is empty or less than 3 characters
                    channelAdapter = new SearchAdapter(mContext, new ArrayList<>());
                    filmAdapter = new SearchFilmsAdapter(mContext, new ArrayList<>());
                    seriesAdapter = new SearchSeriesAdapter(mContext, new ArrayList<>());

                    if (getActivity() != null && isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            binding.chainesRC.setAdapter(channelAdapter);
                            binding.filmsRC.setAdapter(filmAdapter);
                            binding.seriesRC.setAdapter(seriesAdapter);

                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(binding.searchET, InputMethodManager.SHOW_IMPLICIT);
                        });
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (getActivity() != null && isAdded()) {
                    binding.searchET.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(binding.searchET, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });



        return binding.getRoot();
    }
}
