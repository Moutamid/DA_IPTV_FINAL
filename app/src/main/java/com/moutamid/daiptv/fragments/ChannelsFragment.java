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
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
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
                            adapter = new ChannelsAdapter(mContext, channelsList1);
                            binding.channelsRC.setAdapter(adapter);
                            selectedButton.setText("All - " + channelsList1.size());
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
                binding.sidePanel.addView(button);
            }
        }

        setButtonText(list, 2);

        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.CHANNELS_ALL, ChannelsModel.class);
        adapter = new ChannelsAdapter(mContext, channelsList);
        binding.channelsRC.setAdapter(adapter);
    }

    private void setButtonText(ArrayList<CategoryModel> buttons, int buttonCount) {
        if (buttonCount <= buttons.size() - 1) {
            String url = ApiLinks.getLiveStreamsByID(buttons.get(buttonCount).category_id);
            new Thread(() -> {
                URL google = null;
                try {
                    google = new URL(url);
                } catch (final MalformedURLException e) {
                    e.printStackTrace();
                }
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                String input = null;
                StringBuffer stringBuffer = new StringBuffer();
                while (true) {
                    try {
                        if ((input = in != null ? in.readLine() : null) == null) break;
                    } catch (final IOException e) {
                        e.printStackTrace();
                    }
                    stringBuffer.append(input);
                }
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                String htmlData = stringBuffer.toString();
                try {
                    JSONArray response = new JSONArray(htmlData);
                    int size = response.length();
                    requireActivity().runOnUiThread(() -> {
                        for (int i = 0; i < binding.sidePanel.getChildCount(); i++) {
                            View view = binding.sidePanel.getChildAt(i);
                            if (view instanceof MaterialButton) {
                                MaterialButton button = (MaterialButton) view;
                                String enteredText = button.getText().toString();
                                String original = buttons.get(buttonCount).category_name;
                                if (!enteredText.isEmpty() && enteredText.equals(original)) {
                                    button.setText(original + " - " + size);
                                }
                            }
                        }
                        setButtonText(buttons, buttonCount + 1);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }).start();
        }
    }

    private MaterialButton selectedButton = null;

    private void addButton() {
        dialog.show();
        ArrayList<CategoryModel> list = new ArrayList<>();
        list.add(0, new CategoryModel("recent", "Chaînes récentes", 0));
        list.add(1, new CategoryModel("fav", "Favoris", 0));
        list.add(2, new CategoryModel("all", "All", 0));
        String url = ApiLinks.getLiveCategories();

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(url);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();
            try {
                JSONArray response = new JSONArray(htmlData);
                for (int i = 0; i < response.length(); i++) {
                    JSONObject object = response.getJSONObject(i);
                    CategoryModel model = new CategoryModel();
                    model.category_id = object.getString("category_id");
                    model.category_name = object.getString("category_name");
                    model.parent_id = object.getInt("parent_id");
                    list.add(model);
                }
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
                    setButtonText(list, 2);
                    dialog.dismiss();
                });
            } catch (JSONException e) {
                Log.d(TAG, "addButton: EE " + e.getLocalizedMessage());
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).start();
    }

    private void showAllItems() {
        dialog.show();
        ArrayList<ChannelsModel> list = new ArrayList<>();
        String url = ApiLinks.getLiveStreams();
        Log.d(TAG, "showAllItems: " + url);
        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(url);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONArray response = new JSONArray(htmlData);
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
                requireActivity().runOnUiThread(() -> {
                    adapter = new ChannelsAdapter(mContext, list);
                    binding.channelsRC.setAdapter(adapter);
                    dialog.dismiss();
                });
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void switchGroup(String id, String name) {
        dialog.show();
        ArrayList<ChannelsModel> list = new ArrayList<>();
        String url = ApiLinks.getLiveStreamsByID(id);
        Log.d(TAG, "switchGroup: " + url);
        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(url);
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONArray response = new JSONArray(htmlData);
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
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        snackbar = null;
                        Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                    }
                    adapter = new ChannelsAdapter(mContext, list);
                    binding.channelsRC.setAdapter(adapter);
                    String[] n = splitString(name);
                    selectedButton.setText(n[0] + " - " + list.size());
                });
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).start();
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
        adapter = new ChannelsAdapter(mContext, channelsList);
        binding.channelsRC.setAdapter(adapter);
        selectedButton.setText("Favoris - " + channelsList.size());
    }

    private void showRecentChannels() {
        ArrayList<ChannelsModel> channelsList = Stash.getArrayList(Constants.RECENT_CHANNELS, ChannelsModel.class);
        Collections.reverse(channelsList);
        adapter = new ChannelsAdapter(mContext, channelsList);
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
