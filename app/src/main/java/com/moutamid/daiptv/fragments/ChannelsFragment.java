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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.ChannelsAdapter;
import com.moutamid.daiptv.databinding.FragmentChannelsBinding;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChannelsFragment extends Fragment {
    FragmentChannelsBinding binding;
    boolean isAll = true;
    String selectedGroup = "";
    ChannelsAdapter adapter;
    Dialog dialog;
    private RequestQueue requestQueue;
    private static final String TAG = "ChannelsFragment";
    Map<String, String> channels;
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

    public ChannelsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChannelsBinding.inflate(getLayoutInflater(), container, false);

        requestQueue = VolleySingleton.getInstance(mContext).getRequestQueue();

        initializeDialog();

        ArrayList<CategoryModel> list = Stash.getArrayList(Constants.CHANNELS, CategoryModel.class);
        Log.d(TAG, "onCreateView: " + list.size());
        if (list.isEmpty()) addButton();
        else {
            showButtons(list);
            switchGroup(channels.get("FRANCE FHD | TV"));
        }

        return binding.getRoot();
    }

    private void showButtons(ArrayList<CategoryModel> list) {
        channels = new HashMap<>();
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
                button.setStrokeColorResource(R.color.transparent);
                button.setStrokeWidth(2);

                if (selectedButton == null && button.getText().toString().trim().equals("FRANCE FHD | TV")) {
                    button.setStrokeColorResource(R.color.red);
                    selectedButton = button;
                }
                button.setOnClickListener(v -> {
                    isAll = false;
                    selectedGroup = button.getText().toString().trim();
                    switch (selectedGroup) {
                        case "All":
                            ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
                            adapter = new ChannelsAdapter(mContext, channelsList);
                            binding.channelsRC.setAdapter(adapter);
                            break;
                        case "Chaînes récentes":
                            showRecentChannels();
                            break;
                        case "Favoris":
                            showFavoriteChannels();
                            break;
                        default:
                            switchGroup(channels.get(selectedGroup));
                            break;
                    }
                    if (selectedButton != null) {
                        selectedButton.setStrokeColorResource(R.color.transparent);
                    }
                    button.setStrokeColorResource(R.color.red);
                    selectedButton = button;
                });
                binding.sidePanel.addView(button);
//                if (button.getText().toString().equals("All")) {
//                    View view = new View(requireContext());
//                    view.setBackgroundColor(getResources().getColor(R.color.grey2));
//                    binding.sidePanel.addView(view);
//                }
            }
        }

        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
        adapter = new ChannelsAdapter(mContext, channelsList);
        binding.channelsRC.setAdapter(adapter);
    }

    private MaterialButton selectedButton = null;

    private void addButton() {
        dialog.show();
        ArrayList<CategoryModel> list = new ArrayList<>();
        list.add(0, new CategoryModel("recent", "Chaînes récentes", 0));
        list.add(1, new CategoryModel("fav", "Favoris", 0));
        list.add(2, new CategoryModel("all", "All", 0));
        String url = ApiLinks.getLiveCategories();
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = response.getJSONObject(i);
                            CategoryModel model = new CategoryModel();
                            model.category_id = object.getString("category_id");
                            model.category_name = object.getString("category_name");
                            model.parent_id = object.getInt("parent_id");
                            list.add(model);
                        }
                        channels = new HashMap<>();
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
                                }
                                button.setOnClickListener(v -> {
                                    isAll = false;
                                    selectedGroup = button.getText().toString().trim();
                                    switch (selectedGroup) {
                                        case "All":
                                            showAllItems();
                                            break;
                                        case "Chaînes récentes":
                                            showRecentChannels();
                                            break;
                                        case "Favoris":
                                            showFavoriteChannels();
                                            break;
                                        default:
                                            switchGroup(channels.get(selectedGroup));
                                            break;
                                    }
                                    if (selectedButton != null) {
                                        selectedButton.setStrokeColorResource(R.color.transparent); // Remove stroke from previously selected button
                                    }
                                    button.setStrokeColorResource(R.color.red); // Add stroke to newly selected button
                                    selectedButton = button;
                                });
                            }
                        }
                        Stash.put(Constants.CHANNELS, list);
                        showAllItems();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }, error -> {
            error.printStackTrace();
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
    }

    private void showAllItems() {
        dialog.show();
        ArrayList<ChannelsModel> list = new ArrayList<>();
        String url = ApiLinks.getLiveStreams();
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = response.getJSONObject(i);
                            ChannelsModel model = new ChannelsModel();
                            model.num = object.getInt("num");
                            model.stream_id = object.getInt("stream_id");
                            model.name = object.getString("name");
                            model.stream_type = object.getString("stream_type");
                            model.stream_icon = object.getString("stream_icon");
                            model.epg_channel_id = object.getString("epg_channel_id");
                            model.added = object.getString("added");
                            model.category_id = object.getString("category_id");
                            model.stream_link = "";
                            list.add(model);
                        }
                        Stash.put(Constants.CHANNELS_ALL, list);
                        adapter = new ChannelsAdapter(mContext, list);
                        binding.channelsRC.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }, error -> {
            error.printStackTrace();
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
    }

    private void switchGroup(String id) {
        dialog.show();
        ArrayList<ChannelsModel> list = new ArrayList<>();
        String url = ApiLinks.getLiveStreamsByID(id);
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = response.getJSONObject(i);
                            ChannelsModel model = new ChannelsModel();
                            model.num = object.getInt("num");
                            model.stream_id = object.getInt("stream_id");
                            model.name = object.getString("name");
                            model.stream_type = object.getString("stream_type");
                            model.stream_icon = object.getString("stream_icon");
                            model.epg_channel_id = object.getString("epg_channel_id");
                            model.added = object.getString("added");
                            model.category_id = object.getString("category_id");
                            model.stream_link = "";
                            list.add(model);
                        }

                        adapter = new ChannelsAdapter(mContext, list);
                        binding.channelsRC.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }, error -> {
            error.printStackTrace();
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
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
                model.epg_channel_id = favoriteModel.category_id;
                model.category_id = favoriteModel.category_id;
                model.stream_link = favoriteModel.stream_link;
                channelsList.add(model);
            }
        }
        adapter = new ChannelsAdapter(mContext, channelsList);
        binding.channelsRC.setAdapter(adapter);
    }

    private void showRecentChannels() {
        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.RECENT_CHANNELS, ChannelsModel.class);
        Collections.reverse(channelsList);
        adapter = new ChannelsAdapter(mContext, channelsList);
        binding.channelsRC.setAdapter(adapter);
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

    public void refreshList() {
        addButton();
    }
}
