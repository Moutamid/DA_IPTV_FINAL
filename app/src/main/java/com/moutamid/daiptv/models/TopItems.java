package com.moutamid.daiptv.models;

import java.util.ArrayList;

public class TopItems {
    public String name;
    public ArrayList<MovieModel> list;

    public TopItems(String name, ArrayList<MovieModel> list) {
        this.name = name;
        this.list = list;
    }
}
