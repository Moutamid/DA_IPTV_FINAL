package com.moutamid.daiptv.models;

public class EpgListings {
    public String id;
    public String epg_id;
    public String title;
    public String lang;
    public String start;
    public String end;
    public String description;
    public String channel_id;
    public long start_timestamp;
    public long stop_timestamp;
    public int now_playing;
    public int has_archive;

    public EpgListings() {
    }
}
