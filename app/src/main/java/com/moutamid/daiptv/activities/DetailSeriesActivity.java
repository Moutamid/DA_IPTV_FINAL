package com.moutamid.daiptv.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.fxn.stash.Stash;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.CastsAdapter;
import com.moutamid.daiptv.databinding.ActivityDetailSeriesBinding;
import com.moutamid.daiptv.glide.SvgSoftwareLayerSetter;
import com.moutamid.daiptv.models.CastModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesInfoModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
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
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class DetailSeriesActivity extends BaseActivity {
    ActivityDetailSeriesBinding binding;
    SeriesModel model;
    Dialog dialog;
    MovieModel movieModel;
    private static final String TAG = "DetailSeriesActivity";
    ArrayList<CastModel> cast;
    SeriesInfoModel infoModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailSeriesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        model = (SeriesModel) Stash.getObject(Constants.PASS_SERIES, SeriesModel.class);

        cast = new ArrayList<>();

        binding.play.requestFocus();
        binding.back.setOnClickListener(v -> onBackPressed());

        initializeDialog();

        binding.reader.setOnClickListener(v -> {
            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            try {
                SeriesInfoModel seriesInfoModel = (SeriesInfoModel) Stash.getObject(Constants.SERIES_LINK, SeriesInfoModel.class);
                String link = seriesInfoModel == null ?
                        userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + infoModel.id + "." + infoModel.container_extension :
                        userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + seriesInfoModel.id + "." + seriesInfoModel.container_extension;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                intent.setDataAndType(Uri.parse(link), "video/*");
                startActivity(intent);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Échec de la récupération du lien.", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Aucun lecteur externe trouvé", Toast.LENGTH_SHORT).show();
                });
            }
        });

        binding.add.setOnClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.cover;
            favoriteModel.stream_id = Integer.parseInt(infoModel.id);
            favoriteModel.extension = infoModel.container_extension;
            favoriteModel.name = model.name;
            favoriteModel.series_id = model.series_id;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;

            UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
            ArrayList<FavoriteModel> list = Stash.getArrayList(userModel.id, FavoriteModel.class);
            boolean check = list.stream().anyMatch(favorite -> favorite.stream_id == favoriteModel.stream_id);

            if (!check) {
                list.add(favoriteModel);
                Log.d(TAG, "show: " + favoriteModel.stream_id);
                Log.d(TAG, "show: ADDED");
                Stash.put(userModel.id, list);
                Toast.makeText(this, "Ajouté à la liste des favoris", Toast.LENGTH_SHORT).show();
                binding.add.setText("Retirer des favoris");
            } else {
                int index = list.stream()
                        .filter(favorite -> favorite.stream_id == favoriteModel.stream_id)
                        .findFirst()
                        .map(list::indexOf)
                        .orElse(-1);
                if (index != -1) {
                    list.remove(index);
                }
                Stash.put(userModel.id, list);
                binding.add.setText("Ajouter aux favoris");
                Toast.makeText(this, "Supprimé de la liste des favoris", Toast.LENGTH_SHORT).show();
            }
//            new AddFavoriteDialog(this, favoriteModel, check -> {
//                if (check) {
//                    UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
//                    ArrayList<FavoriteModel> filmsList = Stash.getArrayList(userModel.id, FavoriteModel.class);
//                    boolean b = filmsList.stream().anyMatch(favorite -> favorite.stream_id == Integer.parseInt(infoModel.id));
//                    if (b) {
//                        binding.add.setText("Retirer des favoris");
//                    } else {
//                        binding.add.setText("Ajouter aux favoris");
//                    }
//                }
//            }).show();
        });
        AtomicReference<String> seasonKey = new AtomicReference<>("");
        if (model != null) {
            Log.d(TAG, "onCreate  model.series_id : " + model.series_id);
            String url = ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id));
            Log.d(TAG, "fetchID: URL  VOG  " + url);

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
                    JSONObject episodes = response.getJSONObject("episodes");
                    JSONObject info = response.getJSONObject("info");
                    JSONArray seasons = response.getJSONArray("seasons");
                    int lowestSeasonNumber = Integer.MAX_VALUE;
                    for (int i = 0; i < seasons.length(); i++) {
                        JSONObject object = seasons.getJSONObject(i);
                        if (object.getInt("season_number") > 0 && object.getInt("season_number") < lowestSeasonNumber) {
                            lowestSeasonNumber = object.getInt("season_number");
                        }
                    }
                    Log.d(TAG, "lowestSeasonNumber: " + lowestSeasonNumber);
                    try {
                        for (int j = lowestSeasonNumber; j <= seasons.length(); j++) {
                            seasonKey.set(String.valueOf(j));
                            if (episodes.has(seasonKey.get())) {
                                JSONArray array = episodes.getJSONArray(seasonKey.get());
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    int episode_num = object.getInt("episode_num");
                                    if (episode_num == 1) {
                                        infoModel = new SeriesInfoModel(object.getString("id"), object.getString("container_extension"));
                                        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
                                        ArrayList<FavoriteModel> films = Stash.getArrayList(userModel.id, FavoriteModel.class);
                                        boolean check = films.stream().anyMatch(favoriteModel -> favoriteModel.stream_id == Integer.parseInt(infoModel.id));
                                        runOnUiThread(() -> {
                                            if (check) {
                                                binding.add.setText("Retirer des favoris");
                                            } else {
                                                binding.add.setText("Ajouter aux favoris");
                                            }
                                        });
                                        break;
                                    }
                                }
                                break;
                            } else {
                                Log.d(TAG, "onCreate: No value for " + j);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    movieModel = new MovieModel();
                    movieModel.tagline = info.getString("plot");
                    movieModel.original_title = Constants.regexName(info.getString("name"));
                    movieModel.release_date = info.getString("releaseDate");

                    Log.d(TAG, "onCreate: Date " + movieModel.release_date);
                    Log.d(TAG, "onCreate: tagline " + movieModel.tagline);

                    movieModel.overview = info.getString("plot");

                    movieModel.isFrench = !movieModel.tagline.isEmpty();
                    movieModel.vote_average = String.valueOf(info.getInt("rating_5based"));
                    movieModel.genres = info.getString("genre");
                    if (info.getJSONArray("backdrop_path").length() > 0) {
                        movieModel.banner = info.getJSONArray("backdrop_path").getString(0);
                    } else {
                        movieModel.banner = "";
                    }
                    BACKDROP = movieModel.banner;

                    movieModel.trailer = "https://www.youtube.com/watch?v=" + info.getString("youtube_trailer");
                    runOnUiThread(this::setUI);
                    fetchID();
                } catch (JSONException e) {
                    Log.d(TAG, "onCreate: VOG " + e.getLocalizedMessage());
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        dialog.dismiss();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        }
        else {
            Toast.makeText(this, "Chaîne introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.episodes.setOnClickListener(v ->
                startActivity(new Intent(this, SeriesActivity.class)
                .putExtra("IMAGE", logo)
                .putExtra("BACKDROP", BACKDROP)
                .putExtra("SEASON", seasonKey.get())
        ));
    }

    private void fetchID() {
        String name = Constants.regexName(model.name);
        Log.d(TAG, "fetchID: " + name);
        String url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_TV);
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
                int id = object.getInt("id");
                getDetails(id, Constants.lang_fr);
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Film introuvable dans l'API.", Toast.LENGTH_SHORT).show();
                    new MaterialAlertDialogBuilder(this)
                            .setMessage("Aucune série trouvée pour le nom : \"" + name + "\"")
                            .setNegativeButton("Fermer", (dialog1, which) -> dialog1.dismiss())
                            .show();
                });
            }
        }).start();

    }

    private void getDetails(int id, String language) {
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

            try {
                JSONObject response = new JSONObject(htmlData);

                if (response.getString("overview").isEmpty())
                    getDetails(id, "");

                movieModel.isFrench = !movieModel.overview.isEmpty();

                if (movieModel.release_date.isEmpty()) {
                    try {
                        movieModel.release_date = response.getString("release_date");
                    } catch (Exception e) {
                        movieModel.release_date = response.getString("first_air_date");
                    }
                }

                JSONArray credits = response.getJSONObject("credits").getJSONArray("cast");
                JSONArray logos = response.getJSONObject("images").getJSONArray("logos");
                String[] preferredLanguages = {"null", "fr", "en"};
                int logoIndex = -1;
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
                        String path;
                        if (logoIndex != -1) {
                            path = logos.getJSONObject(logoIndex).getString("file_path");
                        } else {
                            path = "";
                        }
                        Log.d(TAG, "getlogo: " + path);
                        logo = Constants.getImageLink(path);
                        runOnUiThread(() -> {
                            binding.name.setVisibility(View.GONE);
                            try {
                                String[] type = logo.split("\\.");
                                if (type[1].equals("svg")) {
                                    RequestBuilder<PictureDrawable> requestBuilder = Glide.with(this)
                                            .as(PictureDrawable.class)
                                            .placeholder(R.color.transparent)
                                            .error(R.color.transparent)
                                            .transition(withCrossFade())
                                            .listener(new SvgSoftwareLayerSetter());
                                    requestBuilder.load(Constants.getImageLink(logo)).into(binding.logo);
                                } else {
                                    Glide.with(this).load(Constants.getImageLink(logo)).placeholder(R.color.transparent).into(binding.logo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        getBackdrop(id, "");
                    }
                cast.clear();
                for (int i = 0; i < credits.length(); i++) {
                    JSONObject object = credits.getJSONObject(i);
                    String name = object.getString("name");
                    String profile_path = object.getString("profile_path");
                    String character = object.getString("character");
                    cast.add(new CastModel(name, character, profile_path));
                }
                runOnUiThread(() -> setUI());
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Aucun contenu trouvé sur le serveur", Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    String logo = "";
    String BACKDROP = "";

    private void getBackdrop(int id, String language) {
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

            try {
                JSONObject jsonObject = new JSONObject(htmlData);
                int logoIndex = -1;

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
                    String path;
                    if (logoIndex != -1) {
                        path = logos.getJSONObject(logoIndex).getString("file_path");
                    } else {
                        path = "";
                    }
                    logo = Constants.getImageLink(path);
                    Log.d(TAG, "getlogo: " + path);
                    runOnUiThread(() -> {
                        binding.name.setVisibility(View.GONE);
                        try {
                            String[] type = path.split("\\.");
                            if (type[1].equals("svg")) {
                                RequestBuilder<PictureDrawable> requestBuilder = Glide.with(this)
                                        .as(PictureDrawable.class)
                                        .placeholder(R.color.transparent)
                                        .error(R.color.transparent)
                                        .transition(withCrossFade())
                                        .listener(new SvgSoftwareLayerSetter());
                                requestBuilder.load(Constants.getImageLink(path)).into(binding.logo);
                            } else {
                                Glide.with(this).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        binding.name.setVisibility(View.VISIBLE);
                        try {
                            Glide.with(DetailSeriesActivity.this).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setUI() {
        dialog.dismiss();
        binding.name.setText(movieModel.original_title);
        binding.desc.setText(movieModel.overview);
        String average = String.format("%.1f", Double.parseDouble(movieModel.vote_average));
        binding.tmdbRating.setText(average);
        binding.filmType.setText(movieModel.genres);

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM yyyy", Locale.FRANCE);

        try {
            Log.d(TAG, "setUI: Date " + movieModel.release_date);
            Date date = inputFormat.parse(movieModel.release_date);
            String formattedDate = outputFormat.format(date);
            String capitalized = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            binding.date.setText(capitalized);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "setUI: " + Constants.getImageLink(movieModel.banner));
        Glide.with(this).load(Constants.getImageLink(movieModel.banner)).into(binding.banner);

        binding.trailer.setOnClickListener(v -> {
            Log.d(TAG, "setUI: " + movieModel.trailer);
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieModel.trailer));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Aucune application trouvée pour ouvrir ce lien", Toast.LENGTH_SHORT).show();
            }
        });

        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);

        binding.play.setOnClickListener(v -> {
            try {
                String link = userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + infoModel.id + "." + infoModel.container_extension;
                String resume = infoModel.id;
                model.extension = infoModel.container_extension;
                Stash.put(Constants.TYPE_SERIES, model);
                startActivity(new Intent(this, VideoPlayerActivity.class)
                        .putExtra("url", link)
                        .putExtra("resume", resume)
                        .putExtra("banner", movieModel.banner)
                        .putExtra("type", Constants.TYPE_SERIES)
                        .putExtra("name", movieModel.original_title));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Échec de la récupération du lien.", Toast.LENGTH_SHORT).show();
                });
            }
        });

        binding.resume.setOnClickListener(v -> {
            try {
                SeriesInfoModel seriesInfoModel = (SeriesInfoModel) Stash.getObject(Constants.SERIES_LINK + infoModel.id, SeriesInfoModel.class);
                String link = seriesInfoModel == null ?
                        userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + infoModel.id + "." + infoModel.container_extension :
                        userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + seriesInfoModel.id + "." + seriesInfoModel.container_extension;
                String resume = seriesInfoModel == null ? infoModel.id : seriesInfoModel.id;
                model.extension = seriesInfoModel == null ? infoModel.container_extension : seriesInfoModel.container_extension;
                Stash.put(Constants.TYPE_SERIES, model);
                startActivity(new Intent(this, VideoPlayerActivity.class)
                        .putExtra("resume", resume)
                        .putExtra("url", link)
                        .putExtra("banner", movieModel.banner)
                        .putExtra("type", Constants.TYPE_SERIES)
                        .putExtra("name", movieModel.original_title));
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Échec de la récupération du lien.", Toast.LENGTH_SHORT).show();
                });
            }
        });

        binding.play.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.add.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.resume.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.trailer.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });
        binding.reader.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.episodes.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });


        CastsAdapter adapter = new CastsAdapter(this, cast);
        binding.castRC.setAdapter(adapter);
    }

    private void initializeDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();
    }

}
