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

    public static String getLiveCategories() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_live_categories;
    }
    public static String getVodCategories() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_vod_categories;
    }
    public static String getSeriesCategories() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_series_categories;
    }
    public static String getLiveStreams() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_live_streams;
    }
    public static String getLiveStreamsByID(String id) {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_live_streams + "&category_id=" + id;
    }
    public static String getVodByID(String id) {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_vod_streams + "&category_id=" + id;
    }
    public static String getVod() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_vod_streams;
    }
    public static String getSeriesByID(String id) {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_series + "&category_id=" + id;
    }
    public static String getSeries() {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_series;
    }
    public static String getSeriesInfoByID(String id) {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_series_info + "&series_id=" + id;
    }
    public static String getVodInfoByID(String id) {
        UserModel userModel = (UserModel) Stash.getObject(Constants.USER, UserModel.class);
        return userModel.url + common + "username=" + userModel.username + "&password=" + userModel.password + get_vod_info + "&vod_id=" + id;
    }
}
