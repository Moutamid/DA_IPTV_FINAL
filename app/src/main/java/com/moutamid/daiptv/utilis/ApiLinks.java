package com.moutamid.daiptv.utilis;

import com.fxn.stash.Stash;
import com.moutamid.daiptv.models.UserModel;

public class ApiLinks {
    public static final String common = "player_api.php?";
    public static final String get_live_categories = "&action=get_live_categories";
    public static final String get_vod_categories = "&action=get_vod_categories";
    public static final String get_series_categories = "&action=get_series_categories";
    public static final String get_live_streams = "&action=get_live_streams";
    public static final String get_vod_streams = "&action=get_vod_streams";
    public static final String get_series = "&action=get_series";
    public static final String get_series_info = "&action=get_series_info";
    public static final String get_vod_info = "&action=get_vod_info";

    public static String base() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url;
    }

    public static String baseUrl() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password;
    }

    public static String basePath() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return common + "username=" + userModel.username + "&password=" + userModel.password;
    }

    public static String vodAll() {
        return basePath() + get_vod_streams;
    }

    public static String seriesAll() {
        return basePath() + get_series;
    }

    public static String vodCategory() {
        return basePath() + get_vod_categories;
    }
    public static String seriesCategory() {
        return basePath() + get_series_categories;
    }
    public static String getVodByID(String id) {
        return basePath() + get_vod_streams + "&category_id=" + id;
    }
    public static String getSeriesByID(String id) {
        return basePath() + get_series + "&category_id=" + id;
    }
    public static String getLiveCategories() {
        return basePath() + get_live_categories;
    }

    public static String getLiveStreams() {
        return basePath() + get_live_streams;
    }
    public static String getLiveStreamsByID(String id) {
        return basePath() + get_live_streams + "&category_id=" + id;
    }
    public static String getSeriesInfoByID(String id) {
        return baseUrl() + get_series_info + "&series_id=" + id;
    }
    public static String getVodInfoByID(String id) {
        return baseUrl() + get_vod_info + "&vod_id=" + id;
    }
}
