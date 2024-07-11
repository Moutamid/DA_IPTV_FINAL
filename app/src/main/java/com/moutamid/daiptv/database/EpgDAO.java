package com.moutamid.daiptv.database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.moutamid.daiptv.models.EPGModel;

import java.util.List;

@Dao
public interface EpgDAO {
    @Insert(onConflict = REPLACE)
    void insert(EPGModel epg);

    @Query("SELECT * FROM epg ORDER BY ID ASC")
    List<EPGModel> getEPG();

    @Query("SELECT * FROM epg WHERE channel = :channel ORDER BY ID ASC")
    List<EPGModel> getTitle(String channel);

    @Query("DELETE FROM epg")
    void Delete();
}
