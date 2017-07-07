package com.easysales.arcgistestapp.api.ArcGISScene;

import com.easysales.arcgistestapp.ApplicationWrapper;
import com.easysales.arcgistestapp.R;
import com.easysales.arcgistestapp.api.OkHttpClientBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by lordp on 06.07.2017.
 */

public class ApiServiceHolder {
    private Retrofit adapter;
    private ApiService apiService;

    private static ApiServiceHolder instance;

    private ApiServiceHolder() {
        adapter = createRestAdapter(ApplicationWrapper.getInstance().getString(R.string.scene_routing_service) + "/");
        apiService = adapter.create(ApiService.class);
    }

    public static ApiServiceHolder getInstance(){
        if(instance == null){
            instance = new ApiServiceHolder();
        }
        return instance;
    }

    public ApiService getApiService(){
        return apiService;
    }

    private Retrofit createRestAdapter(String endPoint){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .client(OkHttpClientBuilder.createOkHttpClient())
                .build();

        return retrofit;
    }
}
