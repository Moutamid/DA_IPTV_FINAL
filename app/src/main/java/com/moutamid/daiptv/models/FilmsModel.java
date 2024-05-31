package com.moutamid.daiptv.models;

import java.util.ArrayList;

public class FilmsModel {
    public String category_id, category_name;
    public ArrayList<VodModel> list;

    public FilmsModel() {
    }

    public FilmsModel(String category_id, String category_name, ArrayList<VodModel> list) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.list = list;
    }

}
