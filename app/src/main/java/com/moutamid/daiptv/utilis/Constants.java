package com.moutamid.daiptv.utilis;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    public static final String FILMS = "FILMS";
    public static final String SERIES = "SERIES";
    public static final String IS_TODAY = "IS_TODAY";
    public static final String HOME = "HOME";
    public static final String CHANNELS = "CHANNELS";
    public static final String CHANNELS_ALL = "CHANNELS_ALL";
    public static final String SERVER_FILM = "SERVER_FILM";
    public static final String SERVER_TV = "SERVER_TV";
    public static final String USER = "USER";
    public static final String RECENT_CHANNELS = "RECENT_CHANNELS";
    public static final String RECENT_CHANNELS_SERVER = "RECENT_CHANNELS_SERVER";
    public static final String USER_LIST = "USER_LIST";
    public static final String EPG = "EPG";
    public static final String SELECTED_PAGE = "SELECTED_PAGE";
    public static final String RECENTS = "RECENTS";
    public static final String TOP_FILMS = "TOP_FILMS";
    public static final String RESUME = "RESUME";
    public static final String topRated = "topRated";
    public static final String TOP_SERIES = "TOP_SERIES";
    public static final String PASS_SERIES = "PASS_SERIES";
    public static final String TYPE_CHANNEL = "channel";
    public static final String TYPE_MOVIE = "movie";
    public static final String TYPE_SERIES = "series";
    public static final String SERIES_INFO = "SERIES_INFO";
    public static final String PASS = "PASS";
    public static final String SERIES_LINK = "SERIES_LINK";
    public static final String TYPE_TV = "tv";
    public static final String TYPE_FILM = "film";
    public static final String lang_fr = "&language=fr";
    public static final String lang_en = "&language=en-US";
    public static final String topTV = "https://api.themoviedb.org/3/tv/popular?api_key=26bedf3e3be75a2810a53f4a445e7b1f&language=fr&page=1";
    public static final String topFILM = "https://api.themoviedb.org/3/movie/popular?api_key=26bedf3e3be75a2810a53f4a445e7b1f&language=fr&page=1";
    public static final String imageLink = "https://image.tmdb.org/t/p/original";
//    https://image.tmdb.org/t/p/original/1sbAPe5cFhayW0NHTqPe3eGc0an.svg;
    public static final String movieSearch = "https://api.themoviedb.org/3/search/";
    public static final String movieDetails = "https://api.themoviedb.org/3/";
    public static final String episodeDetails = "https://api.themoviedb.org/3/tv/";

    public static final String URL_REGEX = "^((https?|ftp)://)?(([a-zA-Z0-9\\-\\.]+)\\.([a-zA-Z]{2,4}))(\\:[0-9]{1,5})?(/.*)?$";

    public static String getImageLink(String path) {
        return imageLink + path;
    }

    public static String getEpisodeDetails(int id, int count) {
        // "https://api.themoviedb.org/3/tv/"+ id +"/season/"+ count +"?language=en-US"
        String api_key = "?api_key=26bedf3e3be75a2810a53f4a445e7b1f";
        return episodeDetails + id + "/season/" + count + api_key + "&language=en-US";
    }

    public static String getMovieData(String name, String year, String type) {
        name = name.replace(" ", "%20");
        String api_key = "&api_key=26bedf3e3be75a2810a53f4a445e7b1f";
        if (year == null) {
            return movieSearch + type + "?query=" + name + api_key + "&include_adult=false&page=1";
        }
        return movieSearch + type + "?query=" + name + api_key + "&include_adult=false&primary_release_year=" + year + "&page=1";
    }

    public static String getMovieDetails(int id, String type, String lang) { // Type movie / tv
        String api_key = "?api_key=26bedf3e3be75a2810a53f4a445e7b1f";
        return movieDetails + type + "/" + id + api_key + "&append_to_response=videos,images,credits" + lang;
    }

    public static boolean checkInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
            return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        }
        return false;
    }

    public static Date parseDate(String dateString) {
        try {
            // 2024-05-30 11:35:00
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss Z", Locale.getDefault());
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isCurrentDateInBetween(Date startDate, Date endDate) {
        Date currentDate = new Date();
        return currentDate.after(startDate) && currentDate.before(endDate);
    }

    public static void checkApp(Activity activity) {
        String appName = "daiptv";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/apps.txt");
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
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                            .setMessage(msg)
                            .setCancelable(false)
                            .show());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static DatabaseReference databaseReference() {
        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("DA_IPTV");
        db.keepSynced(true);
        return db;
    }

    public static void checkFeature(Activity activity, String features) {
        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/Moutamid/Moutamid/main/daiptvlogs");
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
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(features);
                boolean value = myAppObject.getBoolean("value");
                boolean showMessage = myAppObject.getBoolean("showMessage");
                String msg = myAppObject.getString("msg");
                if (value) {
                    if (showMessage)
                        activity.runOnUiThread(() -> new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show());
                    else
                        throw new RuntimeException();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();

    }

    public static String regexName(String name) {
        name = name.replace("VOTSFR", "");
        name = name.replace(" VOTS ", "");
        name = name.replace(" VOT ", "");
        name = name.replace(" H.265 ", "");
        name = name.replace("|PT| ", "");
        name = name.replace("PT| ", "");
        name = name.replace(" PT ", "");
        name = name.replace("|IN| ", "");
        name = name.replace("IN| ", "");
        name = name.replace(" IN ", "");
        name = name.replace("|IT| ", "");
        name = name.replace("IT| ", "");
        name = name.replace(" IT ", "");
        name = name.replace("|FR| ", "");
        name = name.replace("|FR|| ", "");
        name = name.replace("FR| ", "");
        name = name.replace(" FR ", "");
        name = name.replace("|AR| ", "");
        name = name.replace("AR| ", "");
        name = name.replace(" AR ", "");
        name = name.replace("|EN| ", "");
        name = name.replace("EN| ", "");
        name = name.replace(" EN ", "");
        name = name.replace("|ES| ", "");
        name = name.replace("ES| ", "");
        name = name.replace(" ES ", "");
        name = name.replace("|BE| ", "");
        name = name.replace("BE| ", "");
        name = name.replace(" BE ", "");
        name = name.replace("|DE| ", "");
        name = name.replace("DE| ", "");
        name = name.replace(" DE ", "");
        name = name.replace("|PK| ", "");
        name = name.replace("PK| ", "");
        name = name.replace(" PK ", "");
        name = name.replace("|RO| ", "");
        name = name.replace("RO| ", "");
        name = name.replace(" RO ", "");
        name = name.replace("|RU| ", "");
        name = name.replace("RU| ", "");
        name = name.replace(" RU ", "");
        name = name.replace("|BR| ", "");
        name = name.replace("BR| ", "");
        name = name.replace(" BR ", "");
        name = name.replace("|UFC| ", "");
        name = name.replace("UFC| ", "");
        name = name.replace(" UFC ", "");
        name = name.replace(".mkv", "");
        name = name.replace("4K", "");
        name = name.replace("4k", "");
        name = name.replaceAll("\\(\\d{4}\\)", "");
        name = name.replaceAll("\\| \\d{4} \\|", "");
        name = name.replaceAll("\\|\\d{4}\\|", "");
//        name = name.replaceAll("\\|\\|\\d+\\|", "");
//        name = name.replaceAll("\\|", "");
        Pattern pattern = Pattern.compile("\\b\\d{4}\\b");
        Matcher matcher = pattern.matcher(name);
        name = matcher.replaceAll("");
        Pattern patternPattern = Pattern.compile("\\bS\\d{2} E\\d{2}\\b");
        Matcher patternMatcher = patternPattern.matcher(name);
        name = patternMatcher.replaceAll("");
        name = name.replace(" (VOST)", " ");
        name = name.replace(" VOST", " ");
        name = name.replace(" FHD", " ");
        name = name.replace(" (FHD)", " ");
        name = name.replace(" HD", " ");
        name = name.replace(" (HD)", " ");
        name = name.replace(" SD", " ");
        name = name.replace(" (SD)", " ");
        name = name.replace(" MULTI", " ");
        name = name.replace(" (MULTI)", " ");
        name = name.replace(" HEVC", " ");
        name = name.replace(" (HEVC)", " ");
        name = name.replace("(", " ");
        name = name.replace(")", " ");
        name = name.replace(" ( ", " ");
        name = name.replace(" ) ", " ");
        name = name.replace(" | ", " ");
        return name.trim();
    }

    public static String extractYear(String name) {
        String year = null;
        try {
            Pattern pattern = Pattern.compile("\\((\\d{4})\\)|\\| (\\d{4}) \\||\\|(\\d{4})\\||\\b(\\d{4})\\b");
            Matcher matcher = pattern.matcher(name);
            if (matcher.find()) {
                // Extract the year without brackets or pipes
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        year = matcher.group(i);
                        break;
                    }
                }
            } else {
                try {
                    long timestamp = Long.parseLong(name) * 1000L;
                    Date date = new Date(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    year = sdf.format(date);
                } catch (Exception ee) {
                    Log.d(TAG, "extractYear: ERROR " + ee.getLocalizedMessage());
                    try {
                        LocalDate date = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            date = LocalDate.parse(name);
                            year = String.valueOf(date.getYear());
                        }
                    } catch (Exception ex) {
                        Log.d(TAG, "extractYear: " + ex.getLocalizedMessage());
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "extractYear: ERROR " + e.getLocalizedMessage());
            try {
                long timestamp = Long.parseLong(name) * 1000L;
                Date date = new Date(timestamp);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                year = sdf.format(date);
            } catch (Exception ee) {
                Log.d(TAG, "extractYear: ERROR ERROR " + ee.getLocalizedMessage());
                try {
                    LocalDate date = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        date = LocalDate.parse(name);
                        year = String.valueOf(date.getYear());
                    }
                } catch (Exception ex) {
                    Log.d(TAG, "extractYear: " + ex.getLocalizedMessage());
                }
            }
        }
        Log.d(TAG, "extractYear: " + year);
        return year;
    }

    private static final String TAG = "Constants";

    public static String queryName(String channelName) {
        Pattern patternPattern = Pattern.compile("\\bS\\d{2} E\\d{2}\\b");
        Matcher patternMatcher = patternPattern.matcher(channelName);
        channelName = patternMatcher.replaceAll("");
        return channelName.trim();
    }

}
