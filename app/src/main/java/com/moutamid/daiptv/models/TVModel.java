package com.moutamid.daiptv.models;

import java.util.ArrayList;

public class TVModel {
    public String category_id, category_name;
    public ArrayList<SeriesModel> list;

    public TVModel() {
    }

    public TVModel(String category_id, String category_name, ArrayList<SeriesModel> list) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.list = list;
    }

}
