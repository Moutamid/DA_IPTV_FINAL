package com.moutamid.daiptv.fragments;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.caverock.androidsvg.SVG;
import com.fxn.stash.Stash;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.SeriesParentAdapter;
import com.moutamid.daiptv.databinding.FragmentSeriesBinding;
import com.moutamid.daiptv.glide.SvgSoftwareLayerSetter;
import com.moutamid.daiptv.listener.ItemSelectedSeries;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TVModel;
import com.moutamid.daiptv.retrofit.Api;
import com.moutamid.daiptv.retrofit.RetrofitClientInstance;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeriesFragment extends Fragment {
    FragmentSeriesBinding binding;
    SeriesParentAdapter parentAdapter;
    Dialog dialog;
    ArrayList<TVModel> listAll;
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

        topRated = Stash.getArrayList(Constants.TOP_SERIES, SeriesModel.class);
        fetchID(topRated.get(0), true);

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
            listAll = new ArrayList<>();
            listAll.addAll(series);
            parentAdapter = new SeriesParentAdapter(mContext, listAll, selectedFilm);
            binding.recycler.setAdapter(parentAdapter);
            getAllSeries();
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
                    requireActivity().runOnUiThread(() -> {
                        parentAdapter = new SeriesParentAdapter(mContext, listAll, selectedFilm);
                        binding.recycler.setAdapter(parentAdapter);
                        getSeries();
                    });
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

    private void getSeries() {
        getSeriesRecursive(1);
    }

    private void getSeriesRecursive(int k) {
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
                        requireActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            if (snackbar != null) {
                                snackbar.dismiss();
                                snackbar = null;
                                Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                            }
                            Stash.put(Constants.SERIES, listAll);
                            parentAdapter = new SeriesParentAdapter(mContext, listAll, selectedFilm);
                            binding.recycler.setAdapter(parentAdapter);
                            getAllSeries();
                        });
                    } else {
                        getSeriesRecursive(k + 1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SeriesModel>> call, Throwable t) {
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

    private void getAllSeries() {
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<SeriesModel>> call = api.getAllSeries(ApiLinks.seriesAll());
        call.enqueue(new Callback<List<SeriesModel>>() {
            @Override
            public void onResponse(Call<List<SeriesModel>> call, Response<List<SeriesModel>> response) {
                if (response.isSuccessful()) {
                    List<SeriesModel> series = response.body();
                    for (SeriesModel seriesList : series) {
                        seriesList.stream_type = Constants.TYPE_SERIES;
                    }
                    Log.d(TAG, "onResponse: " + series.size());
                    series.sort(Comparator.comparing(seriesModel -> Long.parseLong(seriesModel.last_modified)));
                    Collections.reverse(series);
                    listAll.add(1, new TVModel("Resents", "Récemment ajoutés", (ArrayList<SeriesModel>) series));
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            if (snackbar != null) {
                                snackbar.dismiss();
                            }
                            parentAdapter.notifyItemInserted(1);
                        });
                    }
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<SeriesModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    ItemSelectedSeries selectedFilm = model -> {
        if (model != null) {
            Glide.with(this)
                    .load(R.color.transparent)
                    .into(binding.logo);
            if (!model.stream_type.equals(Constants.topRated)) {
                TranslateAPI type = new TranslateAPI(
                        Language.AUTO_DETECT,
                        Language.ENGLISH,
                        Constants.regexName(model.name));
                type.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        Log.d("TRANSJSILS", "onSuccess: " + translatedText);
                        SeriesModel series = new SeriesModel(model);
                        series.name = translatedText;
                        getFromServer(series);
                    }

                    @Override
                    public void onFailure(String ErrorText) {
                        Log.d(TAG, "onFailure: " + ErrorText);
                    }
                });
            } else {
                fetchID(model, true);
            }
        }
    };

    private void getFromServer(SeriesModel model) {
        RequestQueue requestQueue = VolleySingleton.getInstance(requireContext()).getRequestQueue();
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id)), null,
                response -> {
                    Log.d(TAG, "getFromServer: " + response);
                    try {
                        JSONObject info = response.getJSONObject("info");
                        movieModel = new MovieModel();
                        movieModel.tagline = info.getString("plot");
                        movieModel.original_title = Constants.regexName(info.getString("name"));
                        movieModel.release_date = info.getString("releaseDate");
                        movieModel.overview = info.getString("plot");

                        movieModel.isFrench = !movieModel.tagline.isEmpty();
                        movieModel.vote_average = String.valueOf(info.getInt("rating_5based"));
                        movieModel.genres = info.getString("genre");
                        if (info.getJSONArray("backdrop_path").length() > 0) {
                            movieModel.banner = info.getJSONArray("backdrop_path").getString(0);
                        } else {
                            movieModel.banner = "";
                        }
                        movieModel.trailer = "https://www.youtube.com/watch?v=" + info.getString("youtube_trailer");
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded()) setUI();
                            });
                        }
                        fetchID(model, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(mContext, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(request);
    }

    private void fetchID(SeriesModel model, boolean saveData) {
        Log.d(TAG, "fetchID: saved  " + saveData);
        String name = Constants.regexName(model.name);
        String url;
        url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_TV);

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
                if (array.length() >= 1) {
                    int id = 0;
                    JSONObject object = array.getJSONObject(0);
                    id = object.getInt("id");
                    getDetails(id, Constants.lang_fr, model, saveData);
                } else {
                    if (saveData) {
                        requireActivity().runOnUiThread(() -> {
                            new MaterialAlertDialogBuilder(requireContext())
                                    .setMessage("Aucune série trouvée pour le nom : \"" + name + "\"")
                                    .setNegativeButton("Fermer", (dialog1, which) -> dialog1.dismiss())
                                    .show();
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> dialog.dismiss());
            }

        }).start();
    }

    MovieModel movieModel;
    int count = 0;
    private void getDetails(int id, String language, SeriesModel model, boolean saveData) {
        count++;
        String url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        Log.d("TRANSJSILS", "fetchID: ID  " + id);
        Log.d("TRANSJSILS", "fetchID: URL  " + url);
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
                String[] preferredLanguages = {"null", "fr", "en"};

                if (saveData) {
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
                    movieModel.tagline = response.getString("tagline");
                    if (movieModel.tagline.isEmpty() && !language.isEmpty()  && count < 2)
                        getDetails(id, "", model, saveData);

                    movieModel.isFrench = !movieModel.tagline.isEmpty();
                    movieModel.vote_average = String.valueOf(response.getDouble("vote_average"));
                    if (response.getJSONArray("genres").length() > 0)
                        movieModel.genres = response.getJSONArray("genres").getJSONObject(0).getString("name");
                    else movieModel.genres = "N/A";

                    JSONArray videos = response.getJSONObject("videos").getJSONArray("results");
                    JSONArray images = response.getJSONObject("images").getJSONArray("backdrops");
                    int index = -1;

                    if (images.length() > 0) {
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
                        getBackdrop(id, "", model, saveData);
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
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) setUI();
                        });
                    }
                }

                movieModel.tagline = response.getString("tagline");
                binding.desc.setText(movieModel.tagline);

                int logoIndex = -1;
                JSONArray logos = response.getJSONObject("images").getJSONArray("logos");
                if (logos.length() > 0) {
                    for (String lang : preferredLanguages) {
                        for (int i = 0; i < logos.length(); i++) {
                            JSONObject object = logos.getJSONObject(i);
                            String isoLang = object.getString("iso_639_1");
                            if (isoLang.equalsIgnoreCase(lang)) {
                                logoIndex = i;
                                break;
                            }
                        }
                        if (logoIndex != -1) {
                            break;
                        }
                    }
                    if (logoIndex != -1) {
                        logo = logos.getJSONObject(logoIndex).getString("file_path");
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.name.setVisibility(View.GONE);
                                try {
                                    String[] type = logo.split("\\.");
                                    if (type.length > 1 && type[1].equals("svg")) {
                                        RequestBuilder<PictureDrawable> requestBuilder = Glide.with(this)
                                                .as(PictureDrawable.class)
                                                .placeholder(R.color.transparent)
                                                .error(R.color.transparent)
                                                .transition(withCrossFade())
                                                .listener(new SvgSoftwareLayerSetter());
                                        requestBuilder.load(Constants.getImageLink(logo)).into(binding.logo);
                                    } else {
                                        Glide.with(this)
                                                .load(Constants.getImageLink(logo))
                                                .placeholder(R.color.transparent)
                                                .into(binding.logo);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else {
                        logo = "";
                        if (!saveData) {
                            getBackdrop(id, "", model, saveData);
                        }
                    }
                } else {
                    if (!saveData) {
                        getBackdrop(id, "", model, saveData);
                    }
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.name.setVisibility(View.VISIBLE);
                            try {
                                Glide.with(mContext).load(R.color.transparent).skipMemoryCache(true).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }


                Log.d(TAG, "getDetails: after Back");

            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> dialog.dismiss());
            }
        }).start();
    }

    private void getBackdrop(int id, String language, SeriesModel model, boolean saveData) {
        Log.d(TAG, "getBackdrop: ");
        String url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        Log.d(TAG, "fetchID: ID  " + id);
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
            Log.d(TAG, "getVodRecursive: " + htmlData);

            try {
                JSONObject jsonObject = new JSONObject(htmlData);

                if (saveData) {
                    int index = -1;
                    JSONArray images = jsonObject.getJSONObject("images").getJSONArray("backdrops");
                    if (images.length() > 0) {
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
                        Log.d(TAG, "banner: " + banner);
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
                            });
                        }
                    }
                }

                movieModel.tagline = jsonObject.getString("tagline");

                binding.desc.setText(movieModel.tagline);

                int logoIndex = -1;
                JSONArray logos = jsonObject.getJSONObject("images").getJSONArray("logos");
                Log.d(TAG, "getBackdrop: logos " + logos.length());

                if (logos.length() > 0) {
                    String[] preferredLanguages = {"null", "fr", "en"};
                    for (String lang : preferredLanguages) {
                        for (int i = 0; i < logos.length(); i++) {
                            JSONObject object = logos.getJSONObject(i);
                            String isoLang = object.getString("iso_639_1");
                            if (isoLang.equalsIgnoreCase(lang)) {
                                logoIndex = i;
                                break;
                            }
                        }
                        if (logoIndex != -1) {
                            break;
                        }
                    }
                    if (logoIndex != -1) {
                        logo = logos.getJSONObject(logoIndex).getString("file_path");
                    } else {
                        logo = "";
                    }
                    Log.d(TAG, "getlogo: " + logo);
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.name.setVisibility(View.GONE);
                            try {
                                String[] type = logo.split("\\.");
                                if (type.length > 1 && type[1].equals("svg")) {
                                    RequestBuilder<PictureDrawable> requestBuilder = Glide.with(this)
                                            .as(PictureDrawable.class)
                                            .placeholder(R.color.transparent)
                                            .error(R.color.transparent)
                                            .transition(withCrossFade())
                                            .listener(new SvgSoftwareLayerSetter());
                                    requestBuilder.load(Constants.getImageLink(logo)).into(binding.logo);
                                } else {
                                    Glide.with(this)
                                            .load(Constants.getImageLink(logo))
                                            .placeholder(R.color.transparent)
                                            .into(binding.logo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.name.setVisibility(View.VISIBLE);
                            try {
                                Glide.with(mContext).load(R.color.transparent).skipMemoryCache(true).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    String logo = "";

    private void setUI() {
        try {
            binding.name.setText(movieModel.original_title);
            binding.desc.setText(movieModel.tagline);
            double d = Double.parseDouble(movieModel.vote_average);
            binding.tmdbRating.setText(String.format("%.1f", d));
            binding.filmType.setText(movieModel.genres);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);

            if ((!logo.isEmpty() && movieModel.tagline.isEmpty()) || movieModel.original_title.isEmpty()) {
                Log.d(TAG, "setUI: HIDE");
                binding.synopsis.setVisibility(View.GONE);
            } else {
                binding.synopsis.setVisibility(View.VISIBLE);
            }

            if (movieModel.tagline.isEmpty()) {
                binding.desc.setVisibility(View.GONE);
            } else {
                binding.desc.setVisibility(View.VISIBLE);
            }

            if (movieModel.isFrench) {
                TranslateAPI translateAPI = new TranslateAPI(
                        Language.AUTO_DETECT, Language.FRENCH, movieModel.tagline
                );
                translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        binding.desc.setText(translatedText);
                    }

                    @Override
                    public void onFailure(String ErrorText) {

                    }
                });
            }

            try {
                Date date = inputFormat.parse(movieModel.release_date);
                String formattedDate = outputFormat.format(date);
                String capitalized = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
                binding.date.setText(capitalized);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "setUI: " + movieModel.banner);
            Glide.with(mContext).load(movieModel.banner).placeholder(R.color.transparent).into(binding.banner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Snackbar snackbar;

    public void refreshList() {
        listAll = new ArrayList<>();
        listAll.add(new TVModel(Constants.topRated, "Top Series", topRated));
        snackbar = Snackbar.make(binding.getRoot(), "la playlist est rafraîchissante", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        getCategory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
