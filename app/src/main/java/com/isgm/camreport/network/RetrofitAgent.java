package com.isgm.camreport.network;

import com.google.gson.Gson;
import com.isgm.camreport.network.interceptor.CamReportInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAgent {
    private static RetrofitAgent objInstance;
    private APIService apiService;
    private Retrofit retrofit;

    // Base URL
    private String ACTION_BASE_URL = "http://167.172.70.248:8000";

    private RetrofitAgent() {
        CamReportInterceptor camReportInterceptor = new CamReportInterceptor();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(camReportInterceptor)
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(ACTION_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(new Gson()))
                .client(okHttpClient)
                .build();
        apiService = retrofit.create(APIService.class);
    }

    public static RetrofitAgent getInstance() {
        if (objInstance == null) {
            objInstance = new RetrofitAgent();
        }
        return objInstance;
    }

    public APIService getApiService() {
        return apiService;
    }
}
