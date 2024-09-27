package com.moutamid.daiptv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SeriesActivity extends BaseActivity {
    private static final String TAG = "SeriesActivity";
    ActivitySeriesBinding binding;
    SeriesModel model;
    String output;
    Dialog dialog;
    int id;
    String searchQuery;
    SeriesInfoModel infoModel;
    String SEASON;

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

        binding.seasonsRC.setLayoutManager(new LinearLayoutManager(this));
        binding.seasonsRC.setHasFixedSize(false);

        binding.episodeRC.setLayoutManager(new LinearLayoutManager(this));
        binding.episodeRC.setHasFixedSize(false);

        String IMAGE = getIntent().getStringExtra("IMAGE");
        String BACKDROP = getIntent().getStringExtra("BACKDROP");
        SEASON = getIntent().getStringExtra("SEASON");

        if (IMAGE != null) {
            Glide.with(this).load(IMAGE).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                    binding.logo.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(binding.logo);

            Glide.with(this)
                    .load(BACKDROP)
                    .transform(new CenterCrop(), new BlurTransformation(25, 3))
                    .placeholder(R.color.black)
                    .into(binding.blurredBackground);
        }

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
                JSONObject response = new JSONObject(htmlData);
                JSONArray array = response.getJSONArray("results");
                JSONObject object = array.getJSONObject(0);
                id = object.getInt("id");
                getDetails(id, Integer.parseInt(SEASON), -1);
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Error: " + e.getMessage());
                dialog.dismiss();
            }
        }).start();
    }

    private void getDetails(int id, int count, int episode) {
        String url = Constants.getEpisodeDetails(id, count);
        ArrayList<EpisodesModel> episodesModelArrayList = new ArrayList<>();
        Log.d(TAG, "fetchID: ID  " + id);
        Log.d(TAG, "fetchID: URL EPISODE " + url);
        Log.d(TAG, "fetchID: episode " + episode);

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
                JSONObject response = new JSONObject(htmlData);
                JSONArray episodes = response.getJSONArray("episodes");

                int limit = (episode == -1) ? episodes.length() : episode;

                for (int i = 0; i < Math.min(limit, episodes.length()); i++) {
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
                runOnUiThread(() -> {
                    EpisodesAdapter episodesAdapter = new EpisodesAdapter(SeriesActivity.this, episodesModelArrayList, episodeModel -> {
                        getLink(episodeModel.se);
                    });
                    binding.episodeRC.setAdapter(episodesAdapter);
                });
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "JSONException: " + e.getMessage());
            }
            runOnUiThread(() -> dialog.dismiss());
        }).start();
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
                runOnUiThread(() -> {
                    dialog.dismiss();
                });
                JSONObject response = new JSONObject(htmlData);
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
                runOnUiThread(() -> {
                    startActivity(new Intent(this, VideoPlayerActivity.class)
                            .putExtra("resume", infoModel.id)
                            .putExtra("url", link)
                            .putExtra("banner", "")
                            .putExtra("type", Constants.TYPE_SERIES)
                            .putExtra("name", model.name));
                });
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();

    }

    private void getList() {
        // dialog.show();
        ArrayList<SeasonsItem> seasonSummaries = new ArrayList<>();
        String url = ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id));

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
            runOnUiThread(() -> dialog.dismiss());
            try {
                JSONObject response = new JSONObject(htmlData);
                JSONArray seasons = response.getJSONArray("seasons");
                for (int i = 0; i < seasons.length(); i++) {
                    JSONObject object = seasons.getJSONObject(i);
                    if (object.getInt("season_number") >= Integer.parseInt(SEASON)){
                        SeasonsItem item = new SeasonsItem();
                        item.name = object.getString("name");
                        item.episode_count = object.getString("episode_count");
                        item.season_number = object.getInt("season_number");
                        seasonSummaries.add(item);
                    }
                }
                seasonSummaries.sort(Comparator.comparing(seasonsItem -> seasonsItem.season_number));
                runOnUiThread(() -> {
                    SeasonsAdapter seasonsAdapter = new SeasonsAdapter(this, seasonSummaries, (season, episode) -> {
                        getDetails(id, season, episode);
                    });
                    Log.d(TAG, "seasonSummaries: " + seasonSummaries.size());
                    binding.seasonsRC.setAdapter(seasonsAdapter);
                    getSeasonEpisodes();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();

    }
}
