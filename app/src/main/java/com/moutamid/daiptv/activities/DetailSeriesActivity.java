package com.moutamid.daiptv.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.daiptv.BaseActivity;
import com.moutamid.daiptv.R;
import com.moutamid.daiptv.adapters.CastsAdapter;
import com.moutamid.daiptv.databinding.ActivityDetailSeriesBinding;
import com.moutamid.daiptv.models.CastModel;
import com.moutamid.daiptv.models.FavoriteModel;
import com.moutamid.daiptv.models.MovieModel;
import com.moutamid.daiptv.models.SeriesInfoModel;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.UserModel;
import com.moutamid.daiptv.utilis.AddFavoriteDialog;
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
import java.util.UUID;

public class DetailSeriesActivity extends BaseActivity {
    ActivityDetailSeriesBinding binding;
    SeriesModel model;
    Dialog dialog;
    private RequestQueue requestQueue;
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
//            if (model.() != null) {
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(model.getChannelUrl().trim()));
//                intent.setType("video/*");
//                startActivity(intent);
//            } else {
//                Toast.makeText(this, "Aucun lecteur externe trouvé", Toast.LENGTH_SHORT).show();
//            }
        });

        binding.add.setOnClickListener(v -> {
            FavoriteModel favoriteModel = new FavoriteModel();
            favoriteModel.id = UUID.randomUUID().toString();
            favoriteModel.image = model.cover;
            favoriteModel.name = model.name;
            favoriteModel.category_id = model.category_id;
            favoriteModel.type = model.stream_type;
            new AddFavoriteDialog(this, favoriteModel).show();
        });
        requestQueue = VolleySingleton.getInstance(DetailSeriesActivity.this).getRequestQueue();

        fetchID();
        if (model != null) {
            String url = ApiLinks.getSeriesInfoByID(String.valueOf(model.series_id));
            Log.d(TAG, "fetchID: URL  " + url);
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            JSONObject episodes = response.getJSONObject("episodes");
                            JSONArray array = episodes.getJSONArray("1");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                int episode_num = object.getInt("episode_num");
                                int season = object.getInt("season");
                                if (season == 1 && episode_num == 1) {
                                    infoModel = new SeriesInfoModel(object.getString("id"), object.getString("container_extension"));
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            runOnUiThread(() -> {
                                dialog.dismiss();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        }
                    }, error -> {
                error.printStackTrace();
                runOnUiThread(() -> {
                    dialog.dismiss();
                });
            });
            requestQueue.add(objectRequest);
        } else {
            Toast.makeText(this, "Chaîne introuvable", Toast.LENGTH_SHORT).show();
            finish();
        }

        binding.episodes.setOnClickListener(v -> startActivity(new Intent(this, SeriesActivity.class)));
    }

    private void fetchID() {
        String name = Constants.regexName(model.name);
        Log.d(TAG, "fetchID: " + name);
        String url = Constants.getMovieData(name, Constants.extractYear(model.name), Constants.TYPE_TV);

        Log.d(TAG, "fetchID: URL  " + url);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
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
                }, error -> {
            error.printStackTrace();
            runOnUiThread(() -> {
                dialog.dismiss();
            });
        });
        requestQueue.add(objectRequest);
    }

    private void getDetails(int id, String language) {
        String url = Constants.getMovieDetails(id, Constants.TYPE_TV, language);
        Log.d(TAG, "fetchID: ID  " + id);
        Log.d(TAG, "fetchID: URL  " + url);
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        movieModel = new MovieModel();

                        try {
                            movieModel.original_title = response.getString("original_title");
                        } catch (Exception e) {
                            movieModel.original_title = response.getString("original_name");
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

                        int index = -1;
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

                        setUI();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            dialog.dismiss();
                            Toast.makeText(this, "Aucun contenu trouvé sur le serveur", Toast.LENGTH_LONG).show();
                        });
                    }
                }, error -> {
            error.printStackTrace();
            runOnUiThread(() -> {
                dialog.dismiss();
            });
        });
        requestQueue.add(objectRequest);
    }

    private void getBackdrop(int id, String language) {
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
                            Glide.with(this).load(Constants.getImageLink(movieModel.banner)).placeholder(R.color.transparent).into(binding.banner);
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
        Log.d(TAG, "setUI: " + Constants.getImageLink(movieModel.banner));
        Glide.with(this).load(Constants.getImageLink(movieModel.banner)).into(binding.banner);

        binding.trailer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(movieModel.trailer));
            startActivity(intent);
        });

        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        SeriesInfoModel seriesInfoModel = (SeriesInfoModel) Stash.getObject(Constants.SERIES_LINK, SeriesInfoModel.class);
        String link = seriesInfoModel == null ?
                userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + infoModel.id + "." + infoModel.container_extension :
                userModel.url + "/series/" + userModel.username + "/" + userModel.password + "/" + seriesInfoModel.id + "." + seriesInfoModel.container_extension;

        binding.play.setOnClickListener(v -> {
            startActivity(new Intent(this, VideoPlayerActivity.class).putExtra("url", link).putExtra("name", movieModel.original_title));
        });

        binding.resume.setOnClickListener(v -> {
            String resume = seriesInfoModel == null ? infoModel.id : seriesInfoModel.id;
            startActivity(new Intent(this, VideoPlayerActivity.class).putExtra("resume", resume).putExtra("url", link).putExtra("name", movieModel.original_title));
        });

        binding.play.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
                    binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.add.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
                    binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.resume.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
                    binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.trailer.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
                    binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });
        binding.reader.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
                    binding.nestedScroll.smoothScrollTo(0, -200);
            }
        });

        binding.episodes.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (binding.nestedScroll != null)
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
