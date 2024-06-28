package com.moutamid.daiptv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.EpisodesAdapter;
import com.moutamid.daiptv.adapters.SeasonsAdapter;
import com.moutamid.daiptv.databinding.ActivitySeriesBinding;
import com.moutamid.daiptv.models.EpisodesModel;
import com.moutamid.daiptv.models.SeasonsItem;
import com.moutamid.daiptv.models.SeriesInfoModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SeriesActivity extends BaseActivity {
    private static final String TAG = "SeriesActivity";
    ActivitySeriesBinding binding;
    SeriesModel model;
    String output;
    Dialog dialog;
    RequestQueue requestQueue;
    int id;
    String searchQuery;
    SeriesInfoModel infoModel;

    private void initializeDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = (SeriesModel) Stash.getObject(Constants.PASS_SERIES, SeriesModel.class);

        requestQueue = VolleySingleton.getInstance(this).getRequestQueue();

        binding.seasonsRC.setLayoutManager(new LinearLayoutManager(this));
        binding.seasonsRC.setHasFixedSize(false);

        binding.episodeRC.setLayoutManager(new LinearLayoutManager(this));
        binding.episodeRC.setHasFixedSize(false);

        initializeDialog();

        output = Constants.regexName(model.name);

        getList();

    }

    private void getSeasonEpisodes() {
        fetchID(output);
    }

    private void fetchID(String output) {
        String url;
        url = Constants.getMovieData(output, Constants.extractYear(model.name), Constants.TYPE_TV);

        Log.d(TAG, "fetchID: URL  " + url);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray array = response.getJSONArray("results");
                        JSONObject object = array.getJSONObject(0);
                        id = object.getInt("id");
                        getDetails(id, 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "Error: " + e.getMessage());
                        dialog.dismiss();
                    }
                }, error -> {
            error.printStackTrace();
            Log.d(TAG, "Error: " + error.getMessage());
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
    }

    private void getDetails(int id, int count) {
        String url = Constants.getEpisodeDetails(id, count);
        ArrayList<EpisodesModel> episodesModelArrayList = new ArrayList<>();
        Log.d(TAG, "fetchID: ID  " + id);
        Log.d(TAG, "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    dialog.dismiss();
                    try {
                        JSONArray episodes = response.getJSONArray("episodes");
                        for (int i = 0; i < episodes.length(); i++) {
                            JSONObject object = episodes.getJSONObject(i);
                            int season_number = object.getInt("season_number");
                            int episode_number = object.getInt("episode_number");
                            String name = object.getString("name");
                            String overview = object.getString("overview");
                            String still_path = object.getString("still_path");
                            String se = String.format("S%02d E%02d", season_number, episode_number);
                            EpisodesModel episodesModel = new EpisodesModel(se, name, overview, still_path);
                            episodesModelArrayList.add(episodesModel);
                        }
                        Log.d(TAG, "getDetails: " + episodesModelArrayList.size());
                        EpisodesAdapter episodesAdapter = new EpisodesAdapter(SeriesActivity.this, episodesModelArrayList, episodeModel -> {
                            getLink(episodeModel.se);
                        });
                        binding.episodeRC.setAdapter(episodesAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "JSONException: " + e.getMessage());
                    }
                }, error -> {
            Log.e(TAG, "error: " + error.getMessage());
            error.printStackTrace();
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
    }

    private void getLink(String se) {
        dialog.show();
        String regex = "S(\\d+) E(\\d+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(se);
        if (matcher.find()) {
            int season = Integer.parseInt(matcher.group(1));
            int episodeNumber = Integer.parseInt(matcher.group(2));
            Log.d(TAG, "Season: " + season + ", Episode: " + episodeNumber);
            getInfo(season, episodeNumber);
        }
    }

    private void getInfo(int season, int episode) {
        String url = ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id));
        Log.d(TAG, "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        dialog.dismiss();
                        JSONObject episodes = response.getJSONObject("episodes");
                        JSONArray array = episodes.getJSONArray(String.valueOf(season));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject object = array.getJSONObject(i);
                            int episode_num = object.getInt("episode_num");
                            int season_num = object.getInt("season");
                            if (season_num == season && episode_num == episode) {
                                infoModel = new SeriesInfoModel(object.getString("id"), object.getString("container_extension"));
                                break;
                            }
                        }
                        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                        String link = userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + infoModel.id + "." + infoModel.container_extension;
                        Stash.put(Constants.SERIES_LINK + infoModel.id, infoModel);
                        startActivity(new Intent(this, VideoPlayerActivity.class)
                                .putExtra("resume", infoModel.id)
                                .putExtra("url", link)
                                .putExtra("banner", "")
                                .putExtra("type", Constants.TYPE_SERIES)
                                .putExtra("name", model.name));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                }, error -> {
            error.printStackTrace();
            runOnUiThread(() -> {
                dialog.dismiss();
            });
        });
        requestQueue.add(objectRequest);
    }

    private void getList() {
        // dialog.show();
        ArrayList<SeasonsItem> seasonSummaries = new ArrayList<>();
        String url = ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id));
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray seasons = response.getJSONArray("seasons");
                        for (int i = 0; i < seasons.length(); i++) {
                            JSONObject object = seasons.getJSONObject(i);
                            SeasonsItem item = new SeasonsItem();
                            item.name = object.getString("name");
                            item.episode_count = object.getString("episode_count");
                            item.season_number = object.getInt("season_number");
                            seasonSummaries.add(item);
                        }
                        seasonSummaries.sort(Comparator.comparing(seasonsItem -> seasonsItem.season_number));
                        SeasonsAdapter seasonsAdapter = new SeasonsAdapter(this, seasonSummaries, pos -> {
                            getDetails(id, pos);
                        });
                        Log.d(TAG, "seasonSummaries: " + seasonSummaries.size());
                        binding.seasonsRC.setAdapter(seasonsAdapter);
                        getSeasonEpisodes();
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
}
