package com.moutamid.daiptv.models;

public class UserModel {
    public String id, username, password, url;

    public UserModel() {
    }

    public UserModel(String id, String username, String password, String url) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.url = url;
    }
}
