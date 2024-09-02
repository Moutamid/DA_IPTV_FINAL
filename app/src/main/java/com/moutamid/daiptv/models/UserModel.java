package com.moutamid.daiptv.models;

public class UserModel {
    public String id, name, username, password, url;

    public UserModel() {
    }

    public UserModel(String id, String name, String username, String password, String url) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.url = url;
    }
}
