package com.moutamid.daiptv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.EpisodesAdapter;
import com.moutamid.daiptv.adapters.HomeParentAdapter;
import com.moutamid.daiptv.adapters.SeasonsAdapter;
import com.moutamid.daiptv.databinding.ActivitySeriesBinding;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EpisodesModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeasonsItem;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TopItems;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeriesActivity extends BaseActivity {
    private static final String TAG = "SeriesActivity";
    ActivitySeriesBinding binding;
    SeriesModel model;
    String output;
    Dialog dialog;
    RequestQueue requestQueue;
    int id;
    String searchQuery;

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

        output = model.name;

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
                            Log.d(TAG, "Episode Clicked: ");
                            searchQuery = Constants.queryName(model.name);
                            searchQuery += " " + episodeModel.se;
                            Log.d(TAG, "searchQuery: " + searchQuery);

                           // TODO startActivity(new Intent(SeriesActivity.this, VideoPlayerActivity.class).putExtra("url", channelsModel.getChannelUrl()).putExtra("name", channelsModel.getChannelName()));
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
