package com.moutamid.daiptv.models;

import java.util.List;

public class EpgResponse {
    private List<EpgListings> epg_listings;

    public List<EpgListings> getEpgListings() {
        return epg_listings;
    }

    public void setEpgListings(List<EpgListings> epg_listings) {
        this.epg_listings = epg_listings;
    }
}

