package com.moutamid.daiptv.models;

public class CategoryModel {
    public String category_id, category_name;
    public int parent_id;

    public CategoryModel() {
    }

    public CategoryModel(String category_id, String category_name, int parent_id) {
        this.category_id = category_id;
        this.category_name = category_name;
        this.parent_id = parent_id;
    }
}
