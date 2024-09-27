package com.moutamid.daiptv.activities;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.CastsAdapter;
import com.moutamid.daiptv.databinding.ActivityDetailBinding;
import com.moutamid.daiptv.glide.SvgSoftwareLayerSetter;
import com.moutamid.daiptv.models.CastModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
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
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public class DetailActivity extends BaseActivity {
    private static final String TAG = "DetailActivity";
    ActivityDetailBinding binding;
    VodModel model;
    Dialog dialog;
    MovieModel movieModel;
    ArrayList<CastModel> cast;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.play.setOnFocusChangeListener((v, hasFocus) -> {
            Log.d(TAG, "setUI: hasFocus  " + hasFocus);
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

        model = (VodModel) Stash.getObject(Constants.PASS, VodModel.class);
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        ArrayList<FavoriteModel> films = Stash.getArrayList(userModel.id, FavoriteModel.class);
        boolean check = films.stream().anyMatch(favoriteModel -> favoriteModel.stream_id == model.stream_id);

        if (check) {
            binding.add.setText("Retirer des favoris");
        } else {
            binding.add.setText("Ajouter aux favoris");
        }

        cast = new ArrayList<>();

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.reader.setOnClickListener(v -> {
            String link = userModel.url + "movie/" + userModel.username + "/" + userModel.password + "/" + model.stream_id + "." + model.container_extension;
            Log.d(TAG, "onCreate: link " + link);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            intent.setDataAndType(Uri.parse(link), "video/*");
            startActivity(intent);
        });

        binding.add.setOnClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.stream_icon;
            favoriteModel.name = model.name;
            favoriteModel.extension = model.container_extension;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            favoriteModel.stream_id = model.stream_id;

            ArrayList<FavoriteModel> list = Stash.getArrayList(userModel.id, FavoriteModel.class);
            boolean exist = list.stream().anyMatch(favorite -> favorite.stream_id == favoriteModel.stream_id);

            if (!exist) {
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

//            new AddFavoriteDialog(this, favoriteModel, check1 -> {
//                if (check1) {
//                    ArrayList<FavoriteModel> filmsList = Stash.getArrayList(userModel.id, FavoriteModel.class);
//                    boolean b = filmsList.stream().anyMatch(favorite -> favorite.stream_id == model.stream_id);
//                    if (b) {
//                        binding.add.setText("Retirer des favoris");
//                    } else {
//                        binding.add.setText("Ajouter aux favoris");
//                    }
//                }
//            }).show();

        });

        initializeDialog();

        if (model != null) {
            fetchID();
        } else {
            Toast.makeText(this, "Chaîne introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void fetchID() {
        if (model.stream_id != 0) {
            String url = ApiLinks.getVodInfoByID(String.valueOf(model.stream_id));
            Log.d("TRANSJSILS", "fetchID: URL  " + url);
            getVod(url);
        } else {
            String name = Constants.regexName(model.name);
            Log.d(TAG, "fetchID: " + name);
            String url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_MOVIE);
            Log.d(TAG, "fetchID: " + url);
            getFromTmdb(url);
        }
    }

    private void getFromTmdb(String url) {
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
                });
            }
        }).start();
    }

    private void getVod(String url) {
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
                getDetails(tmdb_id, Constants.lang_fr);
            } catch (JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> dialog.dismiss());
            }

        }).start();
    }

    private void getDetails(int id, String language) {
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
                movieModel.vote_average = String.valueOf(response.getDouble("vote_average"));
                movieModel.genres = response.getJSONArray("genres").getJSONObject(0).getString("name");

                if (movieModel.overview.isEmpty())
                    getDetails(id, "");

                movieModel.isFrench = !movieModel.overview.isEmpty();

                JSONArray videos = response.getJSONObject("videos").getJSONArray("results");
                JSONArray images = response.getJSONObject("images").getJSONArray("backdrops");
                JSONArray credits = response.getJSONObject("credits").getJSONArray("cast");
                JSONArray logos = response.getJSONObject("images").getJSONArray("logos");

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
                        runOnUiThread(() -> {
                            binding.name.setVisibility(View.GONE);
                            try {
                                String[] type = path.split("\\.");
                                if (type[1].equals("svg")) {
                                    RequestBuilder<PictureDrawable> requestBuilder = Glide.with(DetailActivity.this)
                                            .as(PictureDrawable.class)
                                            .placeholder(R.color.transparent)
                                            .error(R.color.transparent)
                                            .transition(withCrossFade())
                                            .listener(new SvgSoftwareLayerSetter());
                                    requestBuilder.load(Constants.getImageLink(path)).into(binding.logo);
                                } else {
                                    Glide.with(DetailActivity.this).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        runOnUiThread(() -> {
                            binding.name.setVisibility(View.VISIBLE);
                            try {
                                Glide.with(DetailActivity.this).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                } else getBackdrop(id, "");

                for (int i = 0; i < videos.length(); i++) {
                    JSONObject object = videos.getJSONObject(i);
                    boolean official = object.getBoolean("official");
                    String type = object.getString("type");
                    if (type.equals("Trailer")) {
                        movieModel.trailer = "https://www.youtube.com/watch?v=" + object.getString("key");
                        break;
                    }
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

    private void getBackdrop(int id, String language) {
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
                    String path;
                    if (logoIndex != -1) {
                        path = logos.getJSONObject(logoIndex).getString("file_path");
                    } else {
                        path = "";
                    }

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
                                Glide.with(DetailActivity.this).load(Constants.getImageLink(path)).placeholder(R.color.transparent).into(binding.logo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    runOnUiThread(() -> {
                        binding.name.setVisibility(View.VISIBLE);
                        try {
                            Glide.with(DetailActivity.this).load(R.color.transparent).placeholder(R.color.transparent).into(binding.logo);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
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
                    runOnUiThread(() -> Glide.with(this).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner));
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
            Date date = inputFormat.parse(movieModel.release_date);
            String formattedDate = outputFormat.format(date);
            String capitalized = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            binding.date.setText(capitalized);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String banner;
        if (Pattern.compile(Constants.URL_REGEX).matcher(movieModel.banner.trim()).matches()) {
            banner = movieModel.banner.trim();
        } else {
            banner = Constants.getImageLink(movieModel.banner.trim());
        }
        Glide.with(this).load(banner).into(binding.banner);
        Log.d(TAG, "setUI: " + banner);

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
        String link = userModel.url + "/movie/" + userModel.username + "/" + userModel.password + "/" + model.stream_id + "." + model.container_extension;
        binding.play.requestFocus();
        binding.play.setOnClickListener(v -> {
            Stash.clear(String.valueOf(model.stream_id));
            Stash.put(Constants.TYPE_MOVIE, model);
            startActivity(new Intent(this, VideoPlayerActivity.class)
                    .putExtra("url", link)
                    .putExtra("banner", movieModel.banner)
                    .putExtra("resume", String.valueOf(model.stream_id))
                    .putExtra("type", Constants.TYPE_MOVIE)
                    .putExtra("name", movieModel.original_title));
        });
        binding.resume.setOnClickListener(v -> {
            Stash.put(Constants.TYPE_MOVIE, model);
            startActivity(new Intent(this, VideoPlayerActivity.class)
                    .putExtra("resume", String.valueOf(model.stream_id))
                    .putExtra("url", link)
                    .putExtra("banner", movieModel.banner)
                    .putExtra("type", Constants.TYPE_MOVIE)
                    .putExtra("name", movieModel.original_title));
        });



        CastsAdapter adapter = new CastsAdapter(this, cast);
        binding.castRC.setAdapter(adapter);

//        try {
//            TranslateAPI translateAPI = new TranslateAPI(
//                    Language.AUTO_DETECT,   //Source Language
//                    Language.FRENCH,         //Target Language
//                    movieModel.overview);           //Query Text
//
//            translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
//                @Override
//                public void onSuccess(String translatedText) {
//                    Log.d(TAG, "onSuccess: " + translatedText);
//                    binding.desc.setText(translatedText);
//                }
//
//                @Override
//                public void onFailure(String ErrorText) {
//                    Log.d(TAG, "onFailure: " + ErrorText);
//                }
//            });
//
//            if (!movieModel.isFrench) {
//                TranslateAPI nameAPI = new TranslateAPI(
//                        Language.AUTO_DETECT,   //Source Language
//                        Language.FRENCH,         //Target Language
//                        movieModel.original_title);           //Query Text
//
//                nameAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
//                    @Override
//                    public void onSuccess(String translatedText) {
//                        Log.d(TAG, "onSuccess: " + translatedText);
//                        binding.name.setText(translatedText);
//                    }
//
//                    @Override
//                    public void onFailure(String ErrorText) {
//                        Log.d(TAG, "onFailure: " + ErrorText);
//                    }
//                });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    private void initializeDialog() {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialog.dismiss();
    }

    private FavoriteModel getFavoriteModel() {
        return null;
    }
}
