package com.moutamid.daiptv.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.ChannelsAdapter;
import com.moutamid.daiptv.adapters.CurrentPrograms;
import com.moutamid.daiptv.databinding.FragmentChannelsBinding;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EpgListings;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.retrofit.Api;
import com.moutamid.daiptv.models.EpgResponse;
import com.moutamid.daiptv.retrofit.RetrofitClientInstance;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChannelsFragment extends Fragment {
    FragmentChannelsBinding binding;
    boolean isAll = true;
    String selectedGroup = "";
    ChannelsAdapter adapter;
    Dialog dialog;
    private static final String TAG = "ChannelsFragment";
    Map<String, String> channels;
    private Context mContext;

    public interface ChannelsListener {
        void getEpg(ChannelsModel model);
    }

    ChannelsListener listener = new ChannelsListener() {
        @Override
        public void getEpg(ChannelsModel model) {
            getAllEpgs(model);
        }
    };

    private void getAllEpgs(ChannelsModel model) {
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        String url = ApiLinks.getDataTable(String.valueOf(model.stream_id));
        Log.d("EPG", "getAllEpgs: " + url);
        Call<EpgResponse> call = api.getEpgListings(url);
        call.enqueue(new Callback<EpgResponse>() {
            @Override
            public void onResponse(Call<EpgResponse> call, Response<EpgResponse> response) {
                if (response.isSuccessful()) {
                    List<EpgListings> epgListings = response.body().getEpgListings();
                    Log.d("EPG", "onResponse: SIZE " + epgListings.size());
                    List<EpgListings> filteredList = epgListings.stream()
                            .filter(channel -> channel.now_playing == 1)
                            .collect(Collectors.toList());
                    Log.d("EPG", "filteredList: " + filteredList.size());
                    showCurrentPrograms(filteredList, model);
                } else {
                    showCurrentPrograms(new ArrayList<>(), model);
                }
            }

            @Override
            public void onFailure(Call<EpgResponse> call, Throwable t) {
                t.printStackTrace();
                Log.d("EPG", "onFailure: " + t.getLocalizedMessage());
                showCurrentPrograms(new ArrayList<>(), model);
            }
        });
    }

    private void showCurrentPrograms(List<EpgListings> filteredList, ChannelsModel model) {
        Dialog currentProgram = new Dialog(requireContext());
        currentProgram.requestWindowFeature(Window.FEATURE_NO_TITLE);
        currentProgram.setContentView(R.layout.current_programs);
        currentProgram.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        currentProgram.setCancelable(true);
        currentProgram.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        currentProgram.show();
        RecyclerView current = currentProgram.findViewById(R.id.currentPrograms);
        TextView nothing = currentProgram.findViewById(R.id.nothing);

        current.setLayoutManager(new LinearLayoutManager(mContext));
        current.setHasFixedSize(false);

        if (filteredList.isEmpty()) {
            nothing.setVisibility(View.VISIBLE);
            current.setVisibility(View.GONE);
        } else {
            nothing.setVisibility(View.GONE);
            current.setVisibility(View.VISIBLE);
        }

        CurrentPrograms programs = new CurrentPrograms(mContext, filteredList, model);
        current.setAdapter(programs);
    }

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

    public ChannelsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChannelsBinding.inflate(getLayoutInflater(), container, false);

        initializeDialog();

        ArrayList<CategoryModel> list = Stash.getArrayList(Constants.CHANNELS, CategoryModel.class);
        Log.d(TAG, "onCreateView: " + list.size());
        if (list.isEmpty()) addButton();
        else {
            showButtons(list);
            switchGroup(channels.get("FRANCE FHD | TV"), "FRANCE FHD | TV");
        }
        return binding.getRoot();
    }

    private void showButtons(ArrayList<CategoryModel> list) {
        channels = new HashMap<>();
        ArrayList<ChannelsModel> channelsList1 = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
        for (CategoryModel model : list) {
            if (!model.category_name.isEmpty()) {
                channels.put(model.category_name.trim(), model.category_id);
                MaterialButton button = new MaterialButton(mContext);
                button.setText(model.category_name.trim());
                if (model.category_name.trim().equals("All")) {
                    button.setText(model.category_name.trim() + " - " + channelsList1.size());
                }
                button.setTextColor(getResources().getColor(R.color.white));
                button.setBackgroundColor(getResources().getColor(R.color.transparent));
                button.setCornerRadius(12);
                button.setNextFocusUpId(R.id.Chaines);
                button.setGravity(Gravity.START | Gravity.CENTER);
                button.setStrokeColorResource(R.color.transparent);
                button.setStrokeWidth(2);

                if (selectedButton == null && button.getText().toString().trim().equals("FRANCE FHD | TV")) {
                    button.setStrokeColorResource(R.color.red);
                    selectedButton = button;
                    selectedButton.requestFocus();
                }
                button.setOnClickListener(v -> {
                    isAll = false;
                    selectedGroup = model.category_name;
                    if (selectedButton != null) {
                        selectedButton.setStrokeColorResource(R.color.transparent);
                    }
                    button.setStrokeColorResource(R.color.red);
                    selectedButton = button;
                    switch (selectedGroup) {
                        case "All":
                            ArrayList<ChannelsModel> allList = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
                            if (allList.isEmpty()) {
                                showAllItems();
                            } else {
                                List<ChannelsModel> filteredList = allList.stream()
                                        .filter(channel -> channel.tv_archive == 1)
                                        .collect(Collectors.toList());
                                Log.d(TAG, "showButtons: " + filteredList.size());
                                Stash.put(Constants.RECENT_CHANNELS_SERVER, filteredList);
                                adapter = new ChannelsAdapter(mContext, allList, null);
                                binding.channelsRC.setAdapter(adapter);
                                selectedButton.setText("All - " + allList.size());
                            }
                            break;
                        case "Chaînes récentes":
                            showRecentChannels();
                            break;
                        case "Rejouer":
                            showRecentsServer();
                            break;
                        case "Favoris":
                            showFavoriteChannels();
                            break;
                        default:
                            switchGroup(channels.get(selectedGroup), selectedGroup);
                            break;
                    }
                });
                binding.sidePanel.addView(button);
            }
        }

        setButtonText(list, 4);

        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
        adapter = new ChannelsAdapter(mContext, channelsList, null);
        binding.channelsRC.setAdapter(adapter);
    }

    private void setButtonText(ArrayList<CategoryModel> buttons, int buttonCount) {
        Log.d(TAG, "setButtonText: " + buttonCount);
        if (buttonCount <= buttons.size() - 1) {
            String url;
            if (buttons.get(buttonCount).category_id.equals("all")) {
                Log.d(TAG, "setButtonText: ALLL");
                url = ApiLinks.getLiveStreams();
            } else {
                url = ApiLinks.getLiveStreamsByID(buttons.get(buttonCount).category_id);
            }
            Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
            Call<List<ChannelsModel>> call = api.getChannels(url);
            call.enqueue(new Callback<List<ChannelsModel>>() {
                @Override
                public void onResponse(Call<List<ChannelsModel>> call, Response<List<ChannelsModel>> response) {
                    if (response.isSuccessful()) {
                        List<ChannelsModel> list = response.body();
                        Log.d(TAG, "onResponse: Size " + list.size());
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                for (int i = 0; i < binding.sidePanel.getChildCount(); i++) {
                                    View view = binding.sidePanel.getChildAt(i);
                                    if (view instanceof MaterialButton) {
                                        MaterialButton button = (MaterialButton) view;
                                        String enteredText = button.getText().toString();
                                        String original = buttons.get(buttonCount).category_name;
                                        if (!enteredText.isEmpty() && enteredText.equals(original)) {
                                            button.setText(original + " - " + list.size());
                                        }
                                    }
                                }
                                setButtonText(buttons, buttonCount + 1);
                            });
                        }

                    }
                }

                @Override
                public void onFailure(Call<List<ChannelsModel>> call, Throwable t) {
                    t.printStackTrace();
                    Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                }
            });
        }
    }

    private MaterialButton selectedButton = null;

    private void addButton() {
        dialog.show();
        ArrayList<CategoryModel> list = new ArrayList<>();
        list.add(0, new CategoryModel("recent_played", "Chaînes récentes", 0));
        list.add(1, new CategoryModel("recent", "Rejouer", 0));
        list.add(2, new CategoryModel("fav", "Favoris", 0));
        list.add(3, new CategoryModel("all", "All", 0));

        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<CategoryModel>> call = api.getChannelsCategory(ApiLinks.getLiveCategories());
        call.enqueue(new Callback<List<CategoryModel>>() {
            @Override
            public void onResponse(Call<List<CategoryModel>> call, Response<List<CategoryModel>> response) {
                if (response.isSuccessful()) {
                    list.addAll(response.body());
                    channels = new HashMap<>();
                    requireActivity().runOnUiThread(() -> {
                        for (CategoryModel model : list) {
                            if (!model.category_name.isEmpty()) {
                                channels.put(model.category_name.trim(), model.category_id);
                                MaterialButton button = new MaterialButton(mContext);
                                button.setText(model.category_name.trim());
                                button.setTextColor(getResources().getColor(R.color.white));
                                button.setBackgroundColor(getResources().getColor(R.color.transparent));
                                button.setCornerRadius(12);
                                button.setNextFocusUpId(R.id.Chaines);
                                button.setGravity(Gravity.START | Gravity.CENTER);
                                binding.sidePanel.addView(button);
                                button.setStrokeColorResource(R.color.transparent);
                                button.setStrokeWidth(2);

                                if (selectedButton == null && button.getText().toString().equals("FRANCE FHD | TV")) {
                                    button.setStrokeColorResource(R.color.red);
                                    selectedButton = button;
                                    selectedButton.requestFocus();
                                }
                                button.setOnClickListener(v -> {
                                    isAll = false;
                                    selectedGroup = model.category_name;
                                    if (selectedButton != null) {
                                        selectedButton.setStrokeColorResource(R.color.transparent); // Remove stroke from previously selected button
                                    }
                                    button.setStrokeColorResource(R.color.red); // Add stroke to newly selected button
                                    selectedButton = button;
                                    switch (selectedGroup) {
                                        case "All":
                                            showAllItems();
                                            break;
                                        case "Rejouer":
                                            showRecentsServer();
                                            break;
                                        case "Chaînes récentes":
                                            showRecentChannels();
                                            break;
                                        case "Favoris":
                                            showFavoriteChannels();
                                            break;
                                        default:
                                            switchGroup(channels.get(selectedGroup), selectedGroup);
                                            break;
                                    }
                                });
                            }
                        }
                        Stash.put(Constants.CHANNELS, list);
                        switchGroup(channels.get("FRANCE FHD | TV"), "FRANCE FHD | TV");
                        setButtonText(list, 4);
                        dialog.dismiss();
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        if (snackbar != null) {
                            snackbar.dismiss();
                            Toast.makeText(mContext, "Error code : " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, "Error : " + t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showRecentsServer() {
        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.RECENT_CHANNELS_SERVER, ChannelsModel.class);
        Collections.reverse(channelsList);
        adapter = new ChannelsAdapter(mContext, channelsList, listener);
        binding.channelsRC.setAdapter(adapter);
        selectedButton.setText("Rejouer - " + channelsList.size());
    }

    private void showAllItems() {
        dialog.show();
        String url = ApiLinks.getLiveStreams();
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<ChannelsModel>> call = api.getChannels(url);
        call.enqueue(new Callback<List<ChannelsModel>>() {
            @Override
            public void onResponse(Call<List<ChannelsModel>> call, Response<List<ChannelsModel>> response) {
                if (response.isSuccessful()) {
                    List<ChannelsModel> list = response.body();
                    Stash.put(Constants.CHANNELS_ALL, list);

                    List<ChannelsModel> filteredList = list.stream()
                            .filter(channel -> channel.tv_archive == 1)
                            .collect(Collectors.toList());

                    Stash.put(Constants.RECENT_CHANNELS_SERVER, filteredList);

                    requireActivity().runOnUiThread(() -> {
                        adapter = new ChannelsAdapter(mContext, (ArrayList<ChannelsModel>) list, null);
                        binding.channelsRC.setAdapter(adapter);
                        dialog.dismiss();
                        selectedButton.setText("All" + " - " + list.size());
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        if (snackbar != null) {
                            snackbar.dismiss();
                            Toast.makeText(mContext, "Error Code : " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ChannelsModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, t.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void switchGroup(String id, String name) {
        dialog.show();
        ArrayList<ChannelsModel> list = new ArrayList<>();
        String url = ApiLinks.getLiveStreamsByID(id);

        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<ChannelsModel>> call = api.getChannels(url);
        call.enqueue(new Callback<List<ChannelsModel>>() {
            @Override
            public void onResponse(Call<List<ChannelsModel>> call, Response<List<ChannelsModel>> response) {
                if (response.isSuccessful()) {
                    List<ChannelsModel> list = response.body();

                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        if (snackbar != null) {
                            snackbar.dismiss();
                            snackbar = null;
                            Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                        }
                        adapter = new ChannelsAdapter(mContext, (ArrayList<ChannelsModel>) list, null);
                        binding.channelsRC.setAdapter(adapter);
                        String[] n = splitString(name);
                        selectedButton.setText(n[0] + " - " + list.size());
                    });
                }
            }

            @Override
            public void onFailure(Call<List<ChannelsModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, t.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public static String[] splitString(String input) {
        // Use regex to find the last numeric part
        String regex = " - (\\d+)$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
        java.util.regex.Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String numericPart = matcher.group(1);
            String alphabeticPart = input.substring(0, matcher.start()).trim();
            return new String[]{alphabeticPart, numericPart};
        } else {
            return new String[]{input};
        }
    }

    private void showFavoriteChannels() {
        ArrayList<ChannelsModel> channelsList = new ArrayList<>();
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> favoriteList = Stash.getArrayList(userModel.id, FavoriteModel.class);
        for (FavoriteModel favoriteModel : favoriteList) {
            if (favoriteModel.type.equals("live")) {
                ChannelsModel model = new ChannelsModel();
                model.name = favoriteModel.name;
                model.stream_type = favoriteModel.type;
                model.stream_icon = favoriteModel.image;
                model.epg_channel_id = favoriteModel.epg_id;
                model.category_id = favoriteModel.category_id;
                model.stream_link = favoriteModel.stream_link;
                channelsList.add(model);
            }
        }
        adapter = new ChannelsAdapter(mContext, channelsList, null);
        binding.channelsRC.setAdapter(adapter);
        selectedButton.setText("Favoris - " + channelsList.size());
    }

    private void showRecentChannels() {
        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.RECENT_CHANNELS, ChannelsModel.class);
        Collections.reverse(channelsList);
        adapter = new ChannelsAdapter(mContext, channelsList, null);
        binding.channelsRC.setAdapter(adapter);
        selectedButton.setText("Chaînes récentes - " + channelsList.size());
    }

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Channels");
    }

    private void initializeDialog() {
        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    Snackbar snackbar;

    public void refreshList() {
        snackbar = Snackbar.make(binding.getRoot(), "la playlist est rafraîchissante", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        addButton();
    }
}
