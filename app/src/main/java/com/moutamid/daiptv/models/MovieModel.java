package com.moutamid.daiptv.models;

public class MovieModel {
    public int id, streamID, series_id;
    public String extension;
    public String original_title, tagline, release_date, overview, trailer, banner, genres, vote_average, type;
    public boolean isFrench;
    public MovieModel() {
    }

    public MovieModel(int id, int streamID, int series_id, String extension, String original_title, String tagline, String release_date, String overview, String trailer, String banner, String genres, String vote_average, String type, boolean isFrench) {
        this.id = id;
        this.streamID = streamID;
        this.series_id = series_id;
        this.extension = extension;
        this.original_title = original_title;
        this.tagline = tagline;
        this.release_date = release_date;
        this.overview = overview;
        this.trailer = trailer;
        this.banner = banner;
        this.genres = genres;
        this.vote_average = vote_average;
        this.type = type;
        this.isFrench = isFrench;
    }
}
