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
import androidx.appcompat.app.AlertDialog;
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
import java.util.Locale;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    static FragmentHomeBinding binding;
    static Dialog dialog;
    static MovieModel movieModel;
    static HomeParentAdapter adapter;
    ArrayList<MovieModel> films = new ArrayList<>(), series = new ArrayList<>();
    static ArrayList<VodModel> filmsChan = new ArrayList<>();
    static ArrayList<SeriesModel> seriesChan = new ArrayList<>();
    static ArrayList<TopItems> list = new ArrayList<>();

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
        list = Stash.getArrayList(Constants.HOME, TopItems.class);
        if (list.isEmpty()) {
            Log.d(TAG, "onCreateView: GET");
            getList();
        } else {
            dialog.show();
            Log.d(TAG, "onCreateView: ELSE");
            fetchID(list.get(0).list.get(0));
            getAllVods();
        }

        return binding.getRoot();
    }

    private void getList() {
        dialog.show();
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
                    dialog.dismiss();
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
                    dialog.dismiss();
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
                dialog.dismiss();
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

                    requireActivity().runOnUiThread(() -> {
                        binding.name.setVisibility(View.GONE);
                        try {
                            Glide.with(mContext).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        binding.name.setVisibility(View.VISIBLE);
                        try {
                            Glide.with(mContext).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

                for (int i = 0; i < videos.length(); i++) {
                    JSONObject object = videos.getJSONObject(i);
                    boolean official = object.getBoolean("official");
                    String Trailer = object.getString("type");
                    if (Trailer.equals("Trailer")) {
                        movieModel.trailer = "https://www.youtube.com/watch?v=" + object.getString("key");
                        break;
                    }
                }
                requireActivity().runOnUiThread(() -> setUI());
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
            }

        }).start();
    }

    private void getBackdrop(int id, String language, MovieModel model) {
        Log.d(TAG, "getBackdrop: ");
        String url;
        if (model.type.equals(Constants.TYPE_SERIES)) {
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
                    requireActivity().runOnUiThread(() -> {
                        Glide.with(mContext).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void setUI() {
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

    @Override
    public void onResume() {
        super.onResume();
        Stash.put(Constants.SELECTED_PAGE, "Home");
        refreshFavoris();
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
        Log.d(TAG, "getAllVods: ");
        String url = ApiLinks.getVod();

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
                ArrayList<MovieModel> vodList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject object = response.getJSONObject(i);
                    MovieModel model = new MovieModel();
                    model.id = object.getInt("num");
                    model.streamID = object.getInt("stream_id");
                    model.original_title = object.getString("name");
                    model.banner = object.getString("stream_icon");
                    model.type = Constants.TYPE_MOVIE + "," + Constants.RECENTS;
                    model.release_date = object.getString("added");
                    model.extension = object.getString("container_extension");
                    vodList.add(model);
                }
                Log.d(TAG, "getAllVods: size " + vodList.size());
                vodList.sort(Comparator.comparing(vodModel -> Long.parseLong(vodModel.release_date)));
                Collections.reverse(vodList);
                list.add(new TopItems("Tous les films", vodList));
                requireActivity().runOnUiThread(() -> {
                    adapter = new HomeParentAdapter(mContext, list, selected);
                    binding.recycler.setAdapter(adapter);
                    getAllStreams();
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void getAllStreams() {
        Log.d(TAG, "getAllStreams: ");
        String url = ApiLinks.getSeries();
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
                JSONArray response = new JSONArray(htmlData);
                ArrayList<MovieModel> seriesList = new ArrayList<>();
                for (int i = 0; i < response.length(); i++) {
                    JSONObject object = response.getJSONObject(i);
                    MovieModel model = new MovieModel();
                    model.id = object.getInt("num");
                    model.series_id = object.getInt("series_id");
                    model.original_title = object.getString("name");
                    model.banner = object.getString("cover");
                    model.overview = object.getString("plot");
                    model.type = Constants.TYPE_SERIES + "," + Constants.RECENTS;
                    model.release_date = object.getString("last_modified");
                    seriesList.add(model);
                }
                seriesList.sort(Comparator.comparing(seriesModel -> Long.parseLong(seriesModel.release_date)));
                Collections.reverse(seriesList);

                list.add(new TopItems("Toutes les séries", seriesList));

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
                    list.add(0, new TopItems("Reprendre la lecture", fvrtList));
                }

                requireActivity().runOnUiThread(() -> {
                    dialog.dismiss();
                    if (snackbar != null) {
                        snackbar.dismiss();
                        snackbar = null;
                        Toast.makeText(mContext, "Actualisation terminée ! Profitez de votre playlist mise à jour.", Toast.LENGTH_SHORT).show();
                    }
                    adapter = new HomeParentAdapter(mContext, list, selected);
                    binding.recycler.setAdapter(adapter);
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
