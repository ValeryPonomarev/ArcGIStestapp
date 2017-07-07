package com.easysales.arcgistestapp.api.ArcGISScene;

import android.util.Log;

import com.easysales.arcgistestapp.api.net.RouteResolveRequest;
import com.easysales.arcgistestapp.api.net.RouteResolveResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by lordp on 06.07.2017.
 */

public class ApiClient {
    private ArrayList<ApiClientCallback> callbacks;

    public ApiClient(){
        callbacks = new ArrayList<>();
    }

    public void routeResolve(RouteResolveRequest routeResolveRequest, final ApiClientCallback callback) {
        final ApiService apiService = ApiServiceHolder.getInstance().getApiService();
        Call<RouteResolveResponse> responceCall = apiService.routeResolve(
                routeResolveRequest.f,
                routeResolveRequest.returnDirections,
                routeResolveRequest.returnRoutes,
                routeResolveRequest.returnZ,
                routeResolveRequest.returnStops,
                routeResolveRequest.returnBarriers,
                routeResolveRequest.returnPolygonBarriers,
                routeResolveRequest.returnPolylineBarriers,
                routeResolveRequest.outSR,
                routeResolveRequest.outputLines,
                routeResolveRequest.restrictionAttributeNames,
                routeResolveRequest.stops);

        responceCall.enqueue(new Callback<RouteResolveResponse>() {
            @Override
            public void onResponse(Call<RouteResolveResponse> call, Response<RouteResolveResponse> response) {
                RouteResolveResponse resolveResponce = response.body();

                //if(response.isSuccessful()){ }

                if(callbacks.contains(callback)){
                    callback.onRouteResolve(resolveResponce, response.code());
                }
            }

            @Override
            public void onFailure(Call<RouteResolveResponse> call, Throwable t) {
                handleError(t, "routeResolve", callback);
            }
        });

    }


    public void attachCallback(ApiClientCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void detachCallback(ApiClientCallback callback) {
        callbacks.remove(callback);
    }


    private void handleError(Throwable t, String methodName, ApiClientCallback callback) {
        Log.e("Api " + methodName + " call", "error " + t.toString(), t);
        if (callbacks.contains(callback)) {
            callback.onError(t);
        }
    }



    public interface ApiClientCallback {
        void onRouteResolve(RouteResolveResponse routeResolveResponse, int code);
        void onError(Throwable throwable);
    }
}
