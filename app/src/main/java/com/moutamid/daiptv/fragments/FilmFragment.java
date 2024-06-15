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
import com.moutamid.daiptv.adapters.FilmParentAdapter;
import com.moutamid.daiptv.databinding.FragmentFilmBinding;
import com.moutamid.daiptv.listener.ItemSelectedFilm;
import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.FilmsModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.VodModel;
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

public class FilmFragment extends Fragment {

    private static final String TAG = "FilmFragment";
    FragmentFilmBinding binding;
    FilmParentAdapter parentAdapter;
    Dialog dialog;
    ArrayList<FilmsModel> listAll;
    private RequestQueue requestQueue;

    public FilmFragment() {
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

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Film");
    }
    ArrayList<VodModel> topRated;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFilmBinding.inflate(getLayoutInflater(), container, false);

        requestQueue = VolleySingleton.getInstance(mContext).getRequestQueue();

        binding.recycler.setLayoutManager(new LinearLayoutManager(mContext));
        binding.recycler.setHasFixedSize(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recycler);

        initializeDialog();

        topRated = Stash.getArrayList(Constants.TOP_FILMS, VodModel.class);
        fetchID(topRated.get(new Random().nextInt(topRated.size())));

        ArrayList<FilmsModel> film = Stash.getArrayList(Constants.FILMS, FilmsModel.class);
        if (film.isEmpty()) {
            listAll = new ArrayList<>();
            listAll.add(new FilmsModel(Constants.topRated, "Top Films", topRated));
            getCategory();
        } else {
            parentAdapter = new FilmParentAdapter(mContext, film, selectedFilm);
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
        String url = ApiLinks.getVodCategories();
        JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject object = response.getJSONObject(i);
                            CategoryModel model = new CategoryModel();
                            model.category_id = object.getString("category_id");
                            model.category_name = object.getString("category_name");
                            model.parent_id = object.getInt("parent_id");
                            listAll.add(new FilmsModel(model.category_id, model.category_name, new ArrayList<>()));
                        }
                        parentAdapter = new FilmParentAdapter(mContext, listAll, selectedFilm);
                        binding.recycler.setAdapter(parentAdapter);
                        getVod();
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

    ItemSelectedFilm selectedFilm = new ItemSelectedFilm() {
        @Override
        public void selected(VodModel model) {
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
        }
    };

    private void fetchID(VodModel model) {
        String name = Constants.regexName(model.name);
        String url;
        url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_MOVIE);
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

    private void getDetails(int id, String language, VodModel model) {
        String url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
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

    private void getBackdrop(int id, String language, VodModel model) {
        Log.d(TAG, "getBackdrop: ");
        String url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
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
        Glide.with(requireContext()).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
    }

    private void getVod() {
        for (int k = 1; k < listAll.size(); k++) {
            FilmsModel items = listAll.get(k);
            String url = ApiLinks.getVodByID(items.category_id);
            int finalK = k;
            JsonArrayRequest objectRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            ArrayList<VodModel> list = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject object = response.getJSONObject(i);
                                VodModel model = new VodModel();
                                model.num = object.getInt("num");
                                model.stream_id = object.getInt("stream_id");
                                model.name = object.getString("name");
                                model.stream_type = object.getString("stream_type");
                                model.stream_icon = object.getString("stream_icon");
                                model.added = object.getString("added");
                                model.category_id = object.getString("category_id");
                                model.container_extension = object.getString("container_extension");
//                                model.rating = object.getDouble("rating");
//                                model.rating_5based = object.getDouble("rating_5based");
                                list.add(model);
                            }
                            FilmsModel model = new FilmsModel(items.category_id, items.category_name, list);
                            listAll.set(finalK, model);
                            if (finalK == listAll.size() - 1) {
                                Stash.put(Constants.FILMS, listAll);
                                dialog.dismiss();
                                parentAdapter = new FilmParentAdapter(mContext, listAll, selectedFilm);
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

    public void refreshList() {
        listAll = new ArrayList<>();
        listAll.add(new FilmsModel(Constants.topRated, "Top Films", topRated));
        getCategory();
    }
}
