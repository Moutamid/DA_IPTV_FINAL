package com.moutamid.daiptv.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.moutamid.daiptv.MainActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.ChannelsAdapter;
import com.moutamid.daiptv.adapters.FilmParentAdapter;
import com.moutamid.daiptv.adapters.SeriesParentAdapter;
import com.moutamid.daiptv.databinding.ActivityLoadingScreenBinding;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.FilmsModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TVModel;
import com.moutamid.daiptv.models.TopItems;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.retrofit.Api;
import com.moutamid.daiptv.retrofit.RetrofitClientInstance;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;

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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoadingScreenActivity extends AppCompatActivity {
    ActivityLoadingScreenBinding binding;
    static ArrayList<TopItems> list = new ArrayList<>();
    ArrayList<MovieModel> films = new ArrayList<>(), series = new ArrayList<>();
    static ArrayList<VodModel> filmsChan = new ArrayList<>();
    static ArrayList<SeriesModel> seriesChan = new ArrayList<>();
    ArrayList<FilmsModel> filmsAll = new ArrayList<>();
    ArrayList<TVModel> listAll = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadingScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String[] messages = {
                "Chargement du contenu .",
                "Chargement du contenu ..",
                "Chargement du contenu ..."
        };
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                binding.message.setText(messages[i]);
                i = (i + 1) % messages.length;
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);

        getList();

    }

    private void getList() {
        String url = Constants.topFILM;

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
                films.clear();
                filmsChan.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);

                    String year = object.getString("release_date");

                    year = year.split("-")[0];
                    VodModel vodModel = new VodModel();
                    vodModel.name = object.getString("title") + " " + year;
                    vodModel.stream_icon = object.getString("poster_path");
                    vodModel.added = object.getString("release_date");
                    vodModel.stream_type = Constants.topRated;
                    vodModel.category_id = String.valueOf(object.getInt("id"));

                    MovieModel model = new MovieModel();
                    model.id = object.getInt("id");
                    model.original_title = object.getString("title") + " " + year;
                    model.banner = object.getString("poster_path");
                    model.type = Constants.TYPE_MOVIE;
                    model.release_date = object.getString("release_date");

                    filmsChan.add(vodModel);
                    films.add(model);
                }
                list.add(new TopItems("Top Films", films));
                Stash.put(Constants.TOP_FILMS, filmsChan);
                getSeries();
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoadingScreenActivity.this, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void getSeries() {
        String url = Constants.topTV;

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
                series.clear();
                seriesChan.clear();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);

                    String year = object.getString("first_air_date");
                    year = year.split("-")[0];

                    MovieModel model = new MovieModel();
                    model.id = object.getInt("id");
                    model.original_title = object.getString("name") + " " + year;
                    model.banner = object.getString("poster_path");
                    model.type = Constants.TYPE_SERIES;
                    model.release_date = object.getString("first_air_date");
                    series.add(model);

                    SeriesModel channel = new SeriesModel();
                    channel.name = object.getString("name") + " " + year;
                    channel.cover = object.getString("poster_path");
                    channel.releaseDate = object.getString("first_air_date");
                    channel.stream_type = Constants.topRated;
                    seriesChan.add(channel);
                }
                list.add(new TopItems("Top Series", series));
                Stash.put(Constants.TOP_SERIES, seriesChan);
                Stash.put(Constants.HOME, list);
                getCategory();
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(LoadingScreenActivity.this, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                });
            }

        }).start();
    }

    private void getCategory() {
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<CategoryModel>> call = api.getVodCategory(ApiLinks.vodCategory());
        call.enqueue(new Callback<List<CategoryModel>>() {
            @Override
            public void onResponse(Call<List<CategoryModel>> call, Response<List<CategoryModel>> response) {
                if (response.isSuccessful()) {
                    List<CategoryModel> categoryList = response.body();
                    filmsAll.addAll(categoryList.stream()
                            .map(model -> new FilmsModel(model.category_id, model.category_name, new ArrayList<>()))
                            .collect(Collectors.toList()));
                    getVod();
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void getVod() {
        Log.d(TAG, "getVod: ");
        getVodRecursive(0);
    }

    private void getVodRecursive(int k) {
        Log.d(TAG, "getVodRecursive: K  " + k);
        if (k >= filmsAll.size()) {
            Log.d(TAG, "getVodRecursive: RETURN");
            return;
        }
        FilmsModel items = filmsAll.get(k);
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<VodModel>> call = api.getVodByID(ApiLinks.getVodByID(items.category_id));
        call.enqueue(new Callback<List<VodModel>>() {
            @Override
            public void onResponse(Call<List<VodModel>> call, Response<List<VodModel>> response) {
                if (response.isSuccessful()) {
                    List<VodModel> list = response.body();
                    list.sort(Comparator.comparingLong(vodModel -> Long.parseLong(vodModel.added)));
                    Collections.reverse(list);
                    FilmsModel model = new FilmsModel(items.category_id, items.category_name, (ArrayList<VodModel>) list);
                    filmsAll.set(k, model);
                    if (k == filmsAll.size() - 1) {
                        ArrayList<VodModel> topRated = Stash.getArrayList(Constants.TOP_FILMS, VodModel.class);
                        filmsAll.add(0, new FilmsModel(Constants.topRated, "Top Films", topRated));
                        Stash.put(Constants.FILMS, filmsAll);
                        getSeriesAll();
                    } else {
                        getVodRecursive(k + 1);
                    }
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                    getVodRecursive(k + 1);
                    runOnUiThread(() -> {
                        Toast.makeText(LoadingScreenActivity.this, "Error Code: " + statusCode , Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onFailure(Call<List<VodModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                runOnUiThread(() -> {
                    Toast.makeText(LoadingScreenActivity.this, t.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void getSeriesAll() {
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<CategoryModel>> call = api.getSeriesCategory(ApiLinks.seriesCategory());
        call.enqueue(new Callback<List<CategoryModel>>() {
            @Override
            public void onResponse(Call<List<CategoryModel>> call, Response<List<CategoryModel>> response) {
                if (response.isSuccessful()) {
                    List<CategoryModel> categoryList = response.body();
                    listAll.addAll(categoryList.stream()
                            .map(model -> new TVModel(model.category_id, model.category_name, new ArrayList<>()))
                            .collect(Collectors.toList()));
                    getSeriesRecursive(0);
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void getSeriesRecursive(int k) {
        Log.d(TAG, "getSeriesRecursive: K " + k);
        if (k >= listAll.size()) {
            return;
        }
        TVModel items = listAll.get(k);
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<SeriesModel>> call = api.getSeriesByID(ApiLinks.getSeriesByID(items.category_id));
        call.enqueue(new Callback<List<SeriesModel>>() {
            @Override
            public void onResponse(Call<List<SeriesModel>> call, Response<List<SeriesModel>> response) {
                if (response.isSuccessful()) {
                    List<SeriesModel> list = response.body();
                    list.sort(Comparator.comparingLong(vodModel -> Long.parseLong(vodModel.last_modified)));
                    Collections.reverse(list);
                    TVModel model = new TVModel(items.category_id, items.category_name, (ArrayList<SeriesModel>) list);
                    listAll.set(k, model);
                    if (k == listAll.size() - 1) {
                        ArrayList<SeriesModel> topRated = Stash.getArrayList(Constants.TOP_SERIES, SeriesModel.class);
                        listAll.add(0,new TVModel(Constants.topRated, "Top Series", topRated));
                        Stash.put(Constants.SERIES, listAll);

                        getChannelsButtons();

                    } else {
                        getSeriesRecursive(k + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SeriesModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                getSeriesRecursive(k + 1);
                runOnUiThread(() -> {
                    Toast.makeText(LoadingScreenActivity.this, t.getLocalizedMessage()+"" , Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void getChannelsButtons() {
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
                    Stash.put(Constants.CHANNELS, list);
                    getAllChannels();
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<CategoryModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private void getAllChannels() {
        String url = ApiLinks.getLiveStreams();
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<ChannelsModel>> call = api.getChannels(url);
        call.enqueue(new Callback<List<ChannelsModel>>() {
            @Override
            public void onResponse(Call<List<ChannelsModel>> call, Response<List<ChannelsModel>> response) {
                if (response.isSuccessful()) {
                    List<ChannelsModel> list = response.body();
                    Stash.put(Constants.CHANNELS_ALL, list);
                    startActivity(new Intent(LoadingScreenActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoadingScreenActivity.this, "Error :", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChannelsModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private static final String TAG = "LoadingScreenActivity";
}