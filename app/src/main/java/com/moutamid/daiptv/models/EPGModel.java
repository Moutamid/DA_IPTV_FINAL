package com.moutamid.daiptv.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "epg")
public class EPGModel {
    @PrimaryKey(autoGenerate = true)
    int ID = 0;

    @ColumnInfo(name = "start")
    String start;

    @ColumnInfo(name = "stop")
    String stop;

    @ColumnInfo(name = "channel")
    String channel;

    @ColumnInfo(name = "title")
    String title;

    public EPGModel(String start, String stop, String channel, String title) {
        this.start = start;
        this.stop = stop;
        this.channel = channel;
        this.title = title;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getStop() {
        return stop;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {this.title = title;}
}
