package com.moutamid.daiptv.models;

import com.google.gson.annotations.SerializedName;

public class VodModel {
    public int num;
    public int stream_id;
    public String name, stream_type, stream_icon, added, category_id, container_extension;
    public VodModel() {
    }

    public VodModel(VodModel other) {
        this.num = other.num;
        this.stream_id = other.stream_id;
        this.name = other.name;
        this.stream_type = other.stream_type;
        this.stream_icon = other.stream_icon;
        this.added = other.added;
        this.category_id = other.category_id;
        this.container_extension = other.container_extension;
    }

}
