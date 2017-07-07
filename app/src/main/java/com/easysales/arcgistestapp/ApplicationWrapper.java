package com.easysales.arcgistestapp;

import android.app.Application;

import com.easysales.arcgistestapp.api.ArcGISScene.ApiClient;
import com.easysales.arcgistestapp.location.LocationHelper;

/**
 * Created by lordp on 23.06.2017.
 */

public class ApplicationWrapper extends Application {
    private static ApplicationWrapper instance;
    private LocationHelper locationHelper;
    private ApiClient sceneApiClient;

    public static ApplicationWrapper getInstance() {
        return instance;
    }

    public LocationHelper getLocationHelper(){
        return locationHelper;
    }

    public ApiClient getSceneApiClient() {
        return sceneApiClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        locationHelper = new LocationHelper();
        sceneApiClient = new ApiClient();
    }
}
