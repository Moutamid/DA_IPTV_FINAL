package com.moutamid.daiptv.retrofit;

import com.moutamid.daiptv.models.CategoryModel;
import com.moutamid.daiptv.models.ChannelsModel;
import com.moutamid.daiptv.models.EpgListings;
import com.moutamid.daiptv.models.SeriesModel;
import com.moutamid.daiptv.models.VodModel;
import com.moutamid.daiptv.models.EpgResponse;
import com.moutamid.daiptv.utilis.ApiLinks;

import org.w3c.dom.Document;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;


public interface Api {
    @GET()
    Call<List<VodModel>> getAllVods(@Url String url);
    @GET()
    Call<List<VodModel>> getVodByID(@Url String url);
    @GET()
    Call<List<CategoryModel>> getVodCategory(@Url String url);
    @GET()
    Call<List<CategoryModel>> getSeriesCategory(@Url String url);
    @GET()
    Call<List<SeriesModel>> getAllSeries(@Url String url);
    @GET()
    Call<List<SeriesModel>> getSeriesByID(@Url String url);
    @GET()
    Call<List<ChannelsModel>> getChannels(@Url String url);
    @GET()
    Call<List<CategoryModel>> getChannelsCategory(@Url String url);
    @GET()
    Call<EpgResponse> getEpgListings(@Url String url);
}
