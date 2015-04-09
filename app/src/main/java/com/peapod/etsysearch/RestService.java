package com.peapod.etsysearch;

import android.app.DownloadManager;

import java.util.Map;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.QueryMap;

/**
 * Rest interface to interact with the Etsy API endpoint
 */
public interface RestService {

    @GET("/listings/active")
    public void getListings(@QueryMap Map<String, String> queryMap, Callback<Listings> cb);

}
