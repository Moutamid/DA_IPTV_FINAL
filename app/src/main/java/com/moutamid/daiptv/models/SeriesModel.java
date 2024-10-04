package com.moutamid.daiptv.models;

import androidx.annotation.NonNull;

import java.util.List;

public class SeriesModel {
    public int num;
    public String name;
    public int series_id;
    public String cover;
    public String extension;
    public String stream_type;
    public String releaseDate;
    public String last_modified;
    public String category_id;

    public SeriesModel() {
    }

    public SeriesModel(SeriesModel other) {
        this.num = other.num;
        this.name = other.name;
        this.series_id = other.series_id;
        this.cover = other.cover;
        this.extension = other.extension;
        this.stream_type = other.stream_type;
        this.releaseDate = other.releaseDate;
        this.last_modified = other.last_modified;
        this.category_id = other.category_id;
    }
}
