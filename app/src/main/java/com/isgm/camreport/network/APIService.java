package com.isgm.camreport.network;

import com.google.gson.JsonArray;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface APIService {
    @GET("/circuits/search")
    Call<ResponseBody> fetchCircuits(@Query("term") String term);

    @GET("/circuits/{circuitId}/areas")
    Call<ResponseBody> fetchAreas(@Path("circuitId") int circuitId);

    @GET("/routes/search")
    Call<ResponseBody> fetchRoutes(@Query("term") String term, @Query("ver_code") String ver_code);

    @POST("/image/upload")
    Call<ResponseBody> uploadImage(@Body JsonArray jsonArray);
}