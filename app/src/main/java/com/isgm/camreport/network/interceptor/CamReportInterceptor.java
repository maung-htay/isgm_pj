package com.isgm.camreport.network.interceptor;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CamReportInterceptor implements Interceptor {

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Log.i("RES", "++++++++Intercepotr+++++++");
        Log.i("RES", request.toString());
        request = request.newBuilder()
                .method(request.method(), request.body()).build();
        return chain.proceed(request);
    }


}
