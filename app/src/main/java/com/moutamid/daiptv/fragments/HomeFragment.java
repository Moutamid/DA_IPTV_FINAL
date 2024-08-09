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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.snackbar.Snackbar;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.HomeParentAdapter;
import com.moutamid.daiptv.databinding.FragmentHomeBinding;
import com.moutamid.daiptv.listener.ItemSelectedHome;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.TopItems;
import com.moutamid.daiptv.models.UserModel;
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    FragmentHomeBinding binding;
    static Dialog loadingBar;
    static MovieModel movieModel;
    static HomeParentAdapter adapter;
    ArrayList<MovieModel> films = new ArrayList<>(), series = new ArrayList<>();
    static ArrayList<VodModel> filmsChan = new ArrayList<>();
    static ArrayList<SeriesModel> seriesChan = new ArrayList<>();
    static ArrayList<TopItems> list = new ArrayList<>();
    UserModel userModel;
    private static Context mContext;

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

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(getLayoutInflater(), container, false);
        initializeDialog();

        binding.recycler.setLayoutManager(new GridLayoutManager(mContext, 1));
        binding.recycler.setHasFixedSize(false);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.recycler);

        return binding.getRoot();
    }

    private void checkData() {
        ArrayList<MovieModel> seriesList = Stash.getArrayList(Constants.SERVER_TV, MovieModel.class);
        ArrayList<MovieModel> moviesList = Stash.getArrayList(Constants.SERVER_FILM, MovieModel.class);
        if (!seriesList.isEmpty() && !moviesList.isEmpty()) {
            list.add(new TopItems("Derniers films ajoutés", moviesList));
            list.add(new TopItems("Dernières séries ajoutées", seriesList));

            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            ArrayList<FavoriteModel> fvrt = Stash.getArrayList(userModel.id, FavoriteModel.class);
            if (!fvrt.isEmpty()) {
                ArrayList<MovieModel> fvrtList = new ArrayList<>();
                for (FavoriteModel channelsModel : fvrt) {
                    if (!channelsModel.type.equals("live")) {
                        MovieModel model = new MovieModel();
                        model.type = channelsModel.type;
                        model.banner = channelsModel.image;
                        model.series_id = channelsModel.series_id;
                        model.extension = channelsModel.extension;
                        model.original_title = channelsModel.name;
                        model.streamID = channelsModel.stream_id;
                        fvrtList.add(model);
                    }
                }
                list.add(new TopItems("Favoris", fvrtList));
            }
            ArrayList<FavoriteModel> films = Stash.getArrayList(Constants.RESUME, FavoriteModel.class);
            ArrayList<MovieModel> fvrtList = new ArrayList<>();
            for (FavoriteModel channelsModel : films) {
                MovieModel model = new MovieModel();
                model.type = channelsModel.type;
                model.banner = channelsModel.image;
                model.original_title = channelsModel.name;
                model.streamID = channelsModel.stream_id;
                fvrtList.add(model);
            }
            if (!fvrtList.isEmpty()) {
                Collections.reverse(fvrtList);
                list.add(2, new TopItems("Reprendre la lecture", fvrtList));
            }

            requireActivity().runOnUiThread(() -> {
                loadingBar.dismiss();
                if (snackbar != null) {
                    snackbar.dismiss();
                    snackbar = null;
                    Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                }
                adapter = new HomeParentAdapter(mContext, list, selected);
                binding.recycler.setAdapter(adapter);
            });
        } else {
            getAllVods();
        }
    }

    private void testingVod() throws UnsupportedEncodingException {
        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Log.d(TAG, "testingVod: " + ApiLinks.vodAll());
        Call<List<VodModel>> call = api.getAllVods(ApiLinks.vodAll());
        call.enqueue(new Callback<List<VodModel>>() {
            @Override
            public void onResponse(Call<List<VodModel>> call, Response<List<VodModel>> response) {
                Log.d(TAG, "onResponse: " + response.toString());
                if (response.isSuccessful()) {
                    List<VodModel> vods = response.body();
                    Log.d(TAG, "onResponse: " + vods.size());
                } else {
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

    private void getList() {
        loadingBar.show();
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
                    Log.d(TAG, "getTopFilms: YEAR " + year);
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
                fetchID(films.get(0));
                Log.d(TAG, "getTopFilms: Films " + films.size());
                list.add(new TopItems("Top Films", films));
                Stash.put(Constants.TOP_FILMS, filmsChan);
                requireActivity().runOnUiThread(() -> {
                    adapter = new HomeParentAdapter(mContext, list, selected);
                    binding.recycler.setAdapter(adapter);
                });
                getSeries();
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    loadingBar.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private void getSeries() {
        Log.d(TAG, "getSeries: ");
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
                getAllVods();
            } catch (JSONException e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    loadingBar.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        Toast.makeText(mContext, e.getLocalizedMessage() + "", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }).start();
    }

    ItemSelectedHome selected = new ItemSelectedHome() {
        @Override
        public void selected(MovieModel model) {
            String[] type = model.type.split(",");
            if (type.length == 2 && type[0].equals(Constants.TYPE_MOVIE)) {
                fetchInfo(model);
            } else {
                fetchID(model);
            }
        }
    };

    private void fetchInfo(MovieModel model) {
        String url = ApiLinks.getVodInfoByID(String.valueOf(model.streamID));
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
                JSONObject info = response.getJSONObject("info");
                int tmdb_id = info.getInt("tmdb_id");
                getDetails(tmdb_id, Constants.lang_fr, model);
            } catch (JSONException e) {
                e.printStackTrace();
                loadingBar.dismiss();
            }
        }).start();
    }

    private void fetchID(MovieModel model) {
        String name = Constants.regexName(model.original_title);
        String url;
        String[] type = model.type.split(",");
        if (type[0].equals(Constants.TYPE_SERIES)) {
            url = Constants.getMovieData(name, Constants.extractYear(model.original_title), Constants.TYPE_TV);
        } else {
            url = Constants.getMovieData(name, Constants.extractYear(model.original_title), Constants.TYPE_MOVIE);
        }
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
                    getDetails(id, Constants.lang_fr, model);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void getDetails(int id, String language, MovieModel model) {
        String url;
        String[] type = model.type.split(",");
        if (type[0].equals(Constants.TYPE_SERIES)) {
            url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        } else {
            url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
        }
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

                int index = -1, logoIndex = -1;
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
                        } else {
                            logo = "";
                        }
                        Log.d(TAG, "getlogo: " + logo);
                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                binding.name.setVisibility(View.GONE);
                                try {
                                    Glide.with(mContext).load(Constants.getImageLink(logo)).placeholder(R.color.transparent).into(binding.logo);
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

                } else {
                    getBackdrop(id, "", model);
                }
                Log.d(TAG, "getDetails: after Back");

                for (int i = 0; i < videos.length(); i++) {
                    JSONObject object = videos.getJSONObject(i);
                    boolean official = object.getBoolean("official");
                    String Trailer = object.getString("type");
                    if (Trailer.equals("Trailer")) {
                        movieModel.trailer = "https://www.youtube.com/watch?v=" + object.getString("key");
                        break;
                    }
                }

                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) setUI();
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
                loadingBar.dismiss();
            }

        }).start();
    }

    String logo = "";

    private void getBackdrop(int id, String language, MovieModel model) {
        Log.d(TAG, "getBackdrop: ");
        String url;
        String[] type = model.type.split(",");
        if (type[0].equals(Constants.TYPE_SERIES)) {
            url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        } else {
            url = Constants.getMovieDetails(id, Constants.TYPE_MOVIE, language);
        }
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

            try {
                JSONObject jsonObject = new JSONObject(htmlData);
                JSONArray images = jsonObject.getJSONObject("images").getJSONArray("backdrops");
                int index = -1, logoIndex = -1;

                JSONArray logos = jsonObject.getJSONObject("images").getJSONArray("logos");
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
                                Glide.with(mContext).load(Constants.getImageLink(logo)).placeholder(R.color.transparent).into(binding.logo);
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
                    if (isAdded()) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
                            });
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private void setUI() {
        binding.name.setText(movieModel.original_title);
        binding.desc.setText(movieModel.tagline);
        double d = Double.parseDouble(movieModel.vote_average);
        binding.tmdbRating.setText(String.format("%.1f", d));
        binding.filmType.setText(movieModel.genres);

        Log.d(TAG, "setUI: original_title " + movieModel.original_title.isEmpty());
        Log.d(TAG, "setUI: tagline " + movieModel.tagline.isEmpty());
        Log.d(TAG, "setUI: logo " + logo.isEmpty());
        Log.d(TAG, "setUI: logo " + logo);

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
        Log.d(TAG, "setUI: " + Constants.getImageLink(logo));
        Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
        Glide.with(mContext).load(Constants.getImageLink(logo)).placeholder(R.color.transparent).into(binding.logo);
    }

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Home");

        userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        list = Stash.getArrayList(Constants.HOME, TopItems.class);
        if (list.isEmpty()) {
            Log.d(TAG, "onCreateView: GET");
            getList();
        } else {
            loadingBar.show();
            Log.d(TAG, "onCreateView: ELSE");
            fetchID(list.get(0).list.get(0));

            long time = Stash.getLong(Constants.IS_TODAY, 0);
            LocalDate date;
            if (time != 0) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    date = Instant.ofEpochMilli(time)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    LocalDate today = LocalDate.now();
                    boolean isToday = date.equals(today);
                    Log.d(TAG, "onCreate: ISTODAY " + isToday);
                    if (!isToday) {
                        getAllVods();
                    } else {
                        checkData();
                    }
                }
            } else {
                getAllVods();
            }
        }

//        refreshFavoris();
    }

    private void initializeDialog() {
        loadingBar = new Dialog(mContext);
        loadingBar.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingBar.setContentView(R.layout.progress_layout);
        loadingBar.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        loadingBar.setCancelable(false);
    }

    Snackbar snackbar;

    public void refreshList() {
        list.clear();
        snackbar = Snackbar.make(binding.getRoot(), "la playlist est rafraîchissante", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        getList();
    }

    public static void refreshFavoris() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> fvrt = Stash.getArrayList(userModel.id, FavoriteModel.class);
        if (!fvrt.isEmpty()) {
            list.remove(list.size() - 1);
            ArrayList<MovieModel> fvrtList = new ArrayList<>();
            for (FavoriteModel channelsModel : fvrt) {
                if (!channelsModel.type.equals("live")) {
                    MovieModel model = new MovieModel();
                    model.type = channelsModel.type;
                    model.banner = channelsModel.image;
                    model.series_id = channelsModel.series_id;
                    model.streamID = channelsModel.stream_id;
                    model.extension = channelsModel.extension;
                    model.original_title = channelsModel.name;
                    fvrtList.add(model);
                }
            }
            list.add(new TopItems("Favoris", fvrtList));
            adapter.notifyItemChanged(list.size() - 1);
        }
    }

    public void getAllVods() {
        loadingBar.show();
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
                    ArrayList<MovieModel> vodList = vods.stream()
                            .limit(50)
                            .map(model -> new MovieModel(
                                    model.num,
                                    model.stream_id,
                                    0,
                                    model.container_extension,
                                    model.name,
                                    "",
                                    model.added,
                                    "",
                                    "",
                                    model.stream_icon,
                                    "",
                                    "",
                                    Constants.TYPE_MOVIE + "," + Constants.RECENTS,
                                    false
                            )).collect(Collectors.toCollection(ArrayList::new));
                    list.add(new TopItems("Derniers films ajoutés", vodList));
                    Stash.put(Constants.SERVER_FILM, vodList);
                    requireActivity().runOnUiThread(() -> {
                        adapter = new HomeParentAdapter(mContext, list, selected);
                        binding.recycler.setAdapter(adapter);
                        getAllStreams();
                    });
                } else {
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

    public void getAllStreams() {
        Log.d(TAG, "getAllStreams: ");

        Api api = RetrofitClientInstance.getRetrofitInstance().create(Api.class);
        Call<List<SeriesModel>> call = api.getAllSeries(ApiLinks.seriesAll());
        call.enqueue(new Callback<List<SeriesModel>>() {
            @Override
            public void onResponse(Call<List<SeriesModel>> call, Response<List<SeriesModel>> response) {
                if (response.isSuccessful()) {
                    List<SeriesModel> series = response.body();
                    Log.d(TAG, "onResponse: series " + series.size());
                    series.sort(Comparator.comparing(seriesModel -> Long.parseLong(seriesModel.last_modified)));
                    Collections.reverse(series);
                    ArrayList<MovieModel> seriesList = series.stream()
                            .limit(50)
                            .map(model -> new MovieModel(
                                    model.num,
                                    0,
                                    model.series_id,
                                    "",
                                    model.name,
                                    "",
                                    model.last_modified,
                                    "",
                                    "",
                                    model.cover,
                                    "",
                                    "",
                                    Constants.TYPE_SERIES + "," + Constants.RECENTS,
                                    false
                            )).collect(Collectors.toCollection(ArrayList::new));

                    Stash.put(Constants.SERVER_TV, seriesList);
                    list.add(new TopItems("Dernières séries ajoutées", seriesList));

                    UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                    ArrayList<FavoriteModel> fvrt = Stash.getArrayList(userModel.id, FavoriteModel.class);
                    if (!fvrt.isEmpty()) {
                        ArrayList<MovieModel> fvrtList = new ArrayList<>();
                        for (FavoriteModel channelsModel : fvrt) {
                            if (!channelsModel.type.equals("live")) {
                                MovieModel model = new MovieModel();
                                model.type = channelsModel.type;
                                model.banner = channelsModel.image;
                                model.series_id = channelsModel.series_id;
                                model.extension = channelsModel.extension;
                                model.original_title = channelsModel.name;
                                fvrtList.add(model);
                            }
                        }
                        list.add(new TopItems("Favoris", fvrtList));
                    }
                    ArrayList<FavoriteModel> films = Stash.getArrayList(Constants.RESUME, FavoriteModel.class);
                    ArrayList<MovieModel> fvrtList = new ArrayList<>();
                    for (FavoriteModel channelsModel : films) {
                        MovieModel model = new MovieModel();
                        model.type = channelsModel.type;
                        model.banner = channelsModel.image;
                        model.original_title = channelsModel.name;
                        model.streamID = channelsModel.stream_id;
                        fvrtList.add(model);
                    }
                    if (!fvrtList.isEmpty()) {
                        Collections.reverse(fvrtList);
                        list.add(2, new TopItems("Reprendre la lecture", fvrtList));
                    }

                    requireActivity().runOnUiThread(() -> {
                        loadingBar.dismiss();
                        if (snackbar != null) {
                            snackbar.dismiss();
                            snackbar = null;
                            Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                        }
                        adapter = new HomeParentAdapter(mContext, list, selected);
                        binding.recycler.setAdapter(adapter);
                    });

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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
