package com.moutamid.daiptv.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.SeriesParentAdapter;
import com.moutamid.daiptv.databinding.FragmentSeriesBinding;
import com.moutamid.daiptv.listener.ItemSelectedSeries;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TVModel;
import com.moutamid.daiptv.utilis.ApiLinks;
import com.moutamid.daiptv.utilis.Constants;
import com.moutamid.daiptv.utilis.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SeriesFragment extends Fragment {
    FragmentSeriesBinding binding;
    SeriesParentAdapter parentAdapter;
    Dialog dialog;
    ArrayList<TVModel> listAll;
    private RequestQueue requestQueue;
    private static final String TAG = "SeriesFragment";

    public SeriesFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Series");
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
    ArrayList<SeriesModel> topRated;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSeriesBinding.inflate(getLayoutInflater(), container, false);

        requestQueue = VolleySingleton.getInstance(mContext).getRequestQueue();

        topRated = Stash.getArrayList(Constants.TOP_SERIES, SeriesModel.class);
        fetchID(topRated.get(new Random().nextInt(topRated.size())));

        binding.recycler.setLayoutManager(new LinearLayoutManager(mContext));
        binding.recycler.setHasFixedSize(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recycler);

        initializeDialog();
        ArrayList<TVModel> series = Stash.getArrayList(Constants.SERIES, TVModel.class);
        if (series.isEmpty()) {
            listAll = new ArrayList<>();
            listAll.add(new TVModel(Constants.topRated, "Top Series", topRated));
            getCategory();
        } else {
            parentAdapter = new SeriesParentAdapter(mContext, series, selectedFilm);
            binding.recycler.setAdapter(parentAdapter);
        }

        return binding.getRoot();
    }

    private void initializeDialog() {
        dialog = new Dialog(mContext);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
    }

    private void getCategory() {
        dialog.show();
        String url = ApiLinks.getSeriesCategories();
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = response.getJSONObject(i);
                            CategoryModel model = new CategoryModel();
                            model.category_id = object.getString("category_id");
                            model.category_name = object.getString("category_name");
                            model.parent_id = object.getInt("parent_id");
                            listAll.add(new TVModel(model.category_id, model.category_name, new ArrayList<>()));
                        }
                        parentAdapter = new SeriesParentAdapter(mContext, listAll, selectedFilm);
                        binding.recycler.setAdapter(parentAdapter);
                        getSeries();
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

    private void getSeries() {
        for (int k = 1; k < listAll.size(); k++) {
            TVModel items = listAll.get(k);
            String url = ApiLinks.getSeriesByID(items.category_id);
            int finalK = k;
            JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            ArrayList<SeriesModel> list = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                SeriesModel model = new SeriesModel();
                                model.num = object.getInt("num");
                                model.series_id = object.getInt("series_id");
                                model.name = object.getString("name");
                                model.cover = object.getString("cover");
                                model.plot = object.getString("plot");
                                model.cast = object.getString("cast");
                                model.director = object.getString("director");
                                model.genre = object.getString("genre");
                                model.releaseDate = object.getString("releaseDate");
                                model.last_modified = object.getString("last_modified");
//                                JSONArray backdrops = object.getJSONArray("backdrop_path");
//                                if (!backdrops.isNull(0)) {
//                                    if (backdrops.length() >= 1) {
//                                        Log.d(TAG, "getSeries: " + backdrops);
//                                        model.backdrop_path = (String) backdrops.get(0);
//                                    }
//                                } else model.backdrop_path = "";
                                model.stream_type = Constants.TYPE_SERIES;
                                model.youtube_trailer = object.getString("youtube_trailer");
                                model.category_id = object.getString("category_id");
                                list.add(model);
                            }
                            TVModel model = new TVModel(items.category_id, items.category_name, list);
                            listAll.set(finalK, model);
                            if (finalK == listAll.size() - 1) {
                                dialog.dismiss();
                                Stash.put(Constants.SERIES, listAll);
                                parentAdapter = new SeriesParentAdapter(mContext, listAll, selectedFilm);
                                binding.recycler.setAdapter(parentAdapter);
                            }
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

    ItemSelectedSeries selectedFilm = model -> {
        if (!model.stream_type.equals(Constants.topRated)) {
            TranslateAPI type = new TranslateAPI(
                    Language.AUTO_DETECT,   //Source Language
                    Language.ENGLISH,         //Target Language
                    Constants.regexName(model.name));           //Query Text

            type.setTranslateListener(new TranslateAPI.TranslateListener() {
                @Override
                public void onSuccess(String translatedText) {
                    Log.d("TRANSJSILS", "onSuccess: " + translatedText);
                    model.name = translatedText;
                    fetchID(model);
                }

                @Override
                public void onFailure(String ErrorText) {
                    Log.d(TAG, "onFailure: " + ErrorText);
                }
            });
        } else fetchID(model);
    };

    private void fetchID(SeriesModel model) {
        String name = Constants.regexName(model.name);
        String url;
        url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_TV);
        Log.d("TRANSJSILS", "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray array = response.getJSONArray("results");
                        if (array.length() >= 1) {
                            int id = 0;
                            JSONObject object = array.getJSONObject(0);
                            id = object.getInt("id");
                            getDetails(id, Constants.lang_fr, model);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                    }
                }, error -> {
            Log.d("TRANSJSILS", "ERROR: " + error.getLocalizedMessage());
            error.printStackTrace();
            dialog.dismiss();
        });
        requestQueue.add(objectRequest);
    }

    MovieModel movieModel;

    private void getDetails(int id, String language, SeriesModel model) {
        String url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        Log.d("TRANSJSILS", "fetchID: ID  " + id);
        Log.d("TRANSJSILS", "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        movieModel = new MovieModel();
                        try {
                            movieModel.original_title = response.getString("title");
                        } catch (Exception e) {
                            movieModel.original_title = response.getString("name");
                        }
                        try {
                            movieModel.release_date = response.getString("release_date");
                        } catch (Exception e) {
                            movieModel.release_date = response.getString("first_air_date");
                        }
                        movieModel.overview = response.getString("overview");

                        if (movieModel.overview.isEmpty() && !language.isEmpty())
                            getDetails(id, "", model);

                        movieModel.isFrench = !movieModel.overview.isEmpty();
                        movieModel.tagline = response.getString("tagline");
                        movieModel.vote_average = String.valueOf(response.getDouble("vote_average"));
                        movieModel.genres = response.getJSONArray("genres").getJSONObject(0).getString("name");

                        JSONArray videos = response.getJSONObject("videos").getJSONArray("results");
                        JSONArray images = response.getJSONObject("images").getJSONArray("backdrops");

                        int index = -1, logoIndex = 0;
                        if (images.length() > 1) {
                            String[] preferredLanguages = {"null", "fr", "en"};
                            for (String lang : preferredLanguages) {
                                for (int i = 0; i < images.length(); i++) {
                                    JSONObject object = images.getJSONObject(i);
                                    String isoLang = object.getString("iso_639_1");
                                    if (isoLang.equalsIgnoreCase(lang)) {
                                        index = i;
                                        break;
                                    }
                                }
                                if (index != -1) {
                                    break;
                                }
                            }
                            String banner = "";
                            if (index != -1) {
                                banner = images.getJSONObject(index).getString("file_path");
                            }
                            movieModel.banner = banner;
                        } else {
                            getBackdrop(id, "", model);
                        }
                        Log.d(TAG, "getDetails: after Back");

                        JSONArray logos = response.getJSONObject("images").getJSONArray("logos");
                        if (logos.length() > 1) {
                            for (int i = 0; i < logos.length(); i++) {
                                JSONObject object = logos.getJSONObject(i);
                                String lang = object.getString("iso_639_1");
                                if (lang.equals("fr") || (logoIndex == 0 && lang.isEmpty())) {
                                    logoIndex = i;
                                    break;
                                } else if (logoIndex == 0 && lang.equals("en")) {
                                    logoIndex = i;
                                }
                            }
                            String path = logos.getJSONObject(logoIndex).getString("file_path");
                            Log.d(TAG, "getlogo: " + path);
                            try {
                                Glide.with(mContext).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            try {
                                Glide.with(mContext).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0; i < videos.length(); i++) {
                            JSONObject object = videos.getJSONObject(i);
                            boolean official = object.getBoolean("official");
                            String type = object.getString("type");
                            if (type.equals("Trailer")) {
                                movieModel.trailer = "https://www.youtube.com/watch?v=" + object.getString("key");
                                break;
                            }
                        }
                        setUI();
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

    private void getBackdrop(int id, String language, SeriesModel model) {
        Log.d(TAG, "getBackdrop: ");
        String url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        Log.d(TAG, "fetchID: ID  " + id);
        Log.d(TAG, "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                jsonObject -> {
                    try {
                        JSONArray images = jsonObject.getJSONObject("images").getJSONArray("backdrops");
                        int index = -1, logoIndex = 0;
                        if (images.length() > 1) {
                            String[] preferredLanguages = {"null", "fr", "en"};
                            for (String lang : preferredLanguages) {
                                for (int i = 0; i < images.length(); i++) {
                                    JSONObject object = images.getJSONObject(i);
                                    String isoLang = object.getString("iso_639_1");
                                    if (isoLang.equalsIgnoreCase(lang)) {
                                        index = i;
                                        break;
                                    }
                                }
                                if (index != -1) {
                                    break;
                                }
                            }
                            String banner = "";
                            if (index != -1) {
                                banner = images.getJSONObject(index).getString("file_path");
                            }
                            movieModel.banner = banner;
                            Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, volleyError -> {
            Log.d(TAG, "getBackdrop: " + volleyError.getLocalizedMessage());
        }
        );
        requestQueue.add(objectRequest);
    }

    private void setUI() {
        binding.name.setText(movieModel.original_title);
        binding.desc.setText(movieModel.tagline);
        double d = Double.parseDouble(movieModel.vote_average);
        binding.tmdbRating.setText(String.format("%.1f", d));
        binding.filmType.setText(movieModel.genres);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);

        try {
            Date date = inputFormat.parse(movieModel.release_date);
            String formattedDate = outputFormat.format(date);
            String capitalized = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            binding.date.setText(capitalized);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "setUI: " + Constants.getImageLink(movieModel.banner));
        Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
    }

    public void refreshList() {
        listAll = new ArrayList<>();
        listAll.add(new TVModel(Constants.topRated, "Top Series", topRated));
        getCategory();
    }
}
