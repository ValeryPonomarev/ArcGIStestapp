package com.easysales.arcgistestapp.api.ArcGISScene;

import com.easysales.arcgistestapp.api.net.RouteResolveRequest;
import com.easysales.arcgistestapp.api.net.RouteResolveResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by lordp on 06.07.2017.
 */

public interface ApiService {
    @GET("solve")
    Call<RouteResolveResponse> routeResolve(@Query("f") String f,
                                            @Query("returnDirections") boolean returnDirections,
                                            @Query("returnRoutes") boolean returnRoutes,
                                            @Query("returnZ") boolean returnZ,
                                            @Query("returnStops") boolean returnStops,
                                            @Query("returnBarriers") boolean returnBarriers,
                                            @Query("returnPolygonBarriers") boolean returnPolygonBarriers,
                                            @Query("returnPolylineBarriers") boolean returnPolylineBarriers,
                                            @Query("outSR") Integer outSR,
                                            @Query("outputLines") String outputLines,
                                            @Query("restrictionAttributeNames") String restrictionAttributeNames,
                                            @Query("stops") RouteResolveRequest.Stops stops);
}
