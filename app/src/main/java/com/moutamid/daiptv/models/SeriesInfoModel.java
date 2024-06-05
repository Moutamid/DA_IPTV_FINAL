package com.moutamid.daiptv.models;

import com.fxn.stash.Stash;

public class SeriesInfoModel {
    public String id, container_extension;

    public SeriesInfoModel(String id, String container_extension) {
        this.id = id;
        this.container_extension = container_extension;
    }
}
