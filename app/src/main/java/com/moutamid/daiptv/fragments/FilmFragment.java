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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.snackbar.Snackbar;
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

public class FilmFragment extends Fragment {

    private static final String TAG = "FilmFragment";
    FragmentFilmBinding binding;
    FilmParentAdapter parentAdapter;
    Dialog dialog;
    ArrayList<FilmsModel> listAll;

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

        binding.recycler.setLayoutManager(new LinearLayoutManager(mContext));
        binding.recycler.setHasFixedSize(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recycler);

        initializeDialog();

        topRated = Stash.getArrayList(Constants.TOP_FILMS, VodModel.class);
        Log.d(TAG, "onCreateView: toprated " + topRated.size());
        ArrayList<FilmsModel> film = Stash.getArrayList(Constants.FILMS, FilmsModel.class);
        Log.d(TAG, "onCreateView: film " + film.size());
        fetchID(topRated.get(0));
        if (film.isEmpty()) {
            listAll = new ArrayList<>();
            listAll.add(new FilmsModel(Constants.topRated, "Top Films", topRated));
            getCategory();
        } else {
            listAll = new ArrayList<>();
            listAll.addAll(film);
            parentAdapter = new FilmParentAdapter(mContext, listAll, selectedFilm);
            binding.recycler.setAdapter(parentAdapter);
            getAllVods();
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
        Call<List<CategoryModel>> call = api.getVodCategory(ApiLinks.vodCategory());
        call.enqueue(new Callback<List<CategoryModel>>() {
            @Override
            public void onResponse(Call<List<CategoryModel>> call, Response<List<CategoryModel>> response) {
                if (response.isSuccessful()) {
                    List<CategoryModel> categoryList = response.body();
                    listAll.addAll(categoryList.stream()
                            .map(model -> new FilmsModel(model.category_id, model.category_name, new ArrayList<>()))
                            .collect(Collectors.toList()));
                    requireActivity().runOnUiThread(() -> {
                        parentAdapter = new FilmParentAdapter(mContext, listAll, selectedFilm);
                        binding.recycler.setAdapter(parentAdapter);
                    });
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
        String url;
        if (model.stream_type.equals(Constants.topRated)) {
            String name = Constants.regexName(model.name);
            url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_MOVIE);
        } else {
            url = ApiLinks.getVodInfoByID(String.valueOf(model.stream_id));
        }
        String finalUrl = url;
        new Thread(() -> {
            URL google = null;
            try {
                google = new URL(finalUrl);
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
                JSONObject response = new JSONObject(htmlData);
                if (model.stream_type.equals(Constants.topRated)) {
                    JSONArray array = response.getJSONArray("results");
                    JSONObject object = array.getJSONObject(0);
                    int id = object.getInt("id");
                    getDetails(id, Constants.lang_fr, model);
                } else {
                    JSONObject info = response.getJSONObject("info");
                    int tmdb_id = info.getInt("tmdb_id");
                    getDetails(tmdb_id, Constants.lang_fr, model);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(requireContext(), "Film introuvable dans l'API.", Toast.LENGTH_SHORT).show();
                });
            }

        }).start();
    }

    MovieModel movieModel;

    private void getDetails(int id, String language, VodModel model) {
        String url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
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
            Log.d(TAG, "getVodRecursive: " + htmlData);

            try {
                JSONObject response = new JSONObject(htmlData);
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
                if (response.getJSONArray("genres").length() > 0)
                    movieModel.genres = response.getJSONArray("genres").getJSONObject(0).getString("name");
                else movieModel.genres = "N/A";

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
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            binding.name.setVisibility(View.GONE);
                            try {
                                Glide.with(mContext).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
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
                                Glide.with(mContext).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
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
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> setUI());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                    });
                }
            }

        }).start();
    }

    private void getBackdrop(int id, String language, VodModel model) {
        Log.d(TAG, "getBackdrop: ");
        String url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
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
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
                        });
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void setUI() {
        try {
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
            try {
                Glide.with(requireContext()).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getVod() {
        Log.d(TAG, "getVod: ");
        getVodRecursive(1);
    }

    private void getVodRecursive(int k) {
        Log.d(TAG, "getVodRecursive: K  " + k);
        if (k >= listAll.size()) {
            Log.d(TAG, "getVodRecursive: RETURN");
            return;
        }
        FilmsModel items = listAll.get(k);
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
                    listAll.set(k, model);
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (k == listAll.size() - 1) {
                                if (snackbar != null) {
                                    snackbar.dismiss();
                                    snackbar = null;
                                    Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                                }
                                Stash.put(Constants.FILMS, listAll);
                                parentAdapter = new FilmParentAdapter(mContext, listAll, selectedFilm);
                                binding.recycler.setAdapter(parentAdapter);
                                getAllVods();
                            } else {
                                getVodRecursive(k + 1);
                            }
                        });
                    }
                } else {
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            if (snackbar != null) {
                                snackbar.dismiss();
                                Toast.makeText(mContext, "Error code : " + statusCode, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<List<VodModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
                if (isAdded() && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        if (snackbar != null) {
                            snackbar.dismiss();
                            Toast.makeText(mContext, t.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    Snackbar snackbar;

    public void refreshList() {
        listAll = new ArrayList<>();
        listAll.add(new FilmsModel(Constants.topRated, "Top Films", topRated));
        snackbar = Snackbar.make(binding.getRoot(), "la playlist est rafraîchissante", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        getCategory();
    }

    public void getAllVods() {
        Log.d(TAG, "getAllVods: ");
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<VodModel>> call = api.getAllVods(ApiLinks.vodAll());
        call.enqueue(new Callback<List<VodModel>>() {
            @Override
            public void onResponse(Call<List<VodModel>> call, Response<List<VodModel>> response) {
                if (response.isSuccessful()) {
                    List<VodModel> vods = response.body();
                    Log.d(TAG, "onResponse: " + vods.size());

                    vods.sort(Comparator.comparing(vodModel -> Long.parseLong(vodModel.added)));
                    Collections.reverse(vods);
                    listAll.add(1, new FilmsModel("Resents", "Récemment ajoutés", (ArrayList<VodModel>) vods));
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            dialog.dismiss();
                            parentAdapter.notifyItemInserted(1);
                        });
                    }
                } else {
                    if (isAdded() && getActivity() != null) {
                        getActivity().runOnUiThread(() -> dialog.dismiss());
                    }
                    int statusCode = response.code();
                    Log.d(TAG, "onResponse: Error code : " + statusCode);
                }
            }

            @Override
            public void onFailure(Call<List<VodModel>> call, Throwable t) {
                t.printStackTrace();
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
