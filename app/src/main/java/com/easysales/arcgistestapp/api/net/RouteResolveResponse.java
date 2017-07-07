package com.easysales.arcgistestapp.api.net;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lordp on 06.07.2017.
 */

public class RouteResolveResponse {
    public Route routes;

    public RouteResolveResponse() {
    }

    public class Route {
        public List<RouteFeature> features;

        public Route() {
            features = new ArrayList<>();
        }
    }

    public class RouteFeature {
        public RouteFeatureGeometry geometry;
    }

    public class RouteFeatureGeometry {
        public boolean hasZ;
        public List<List<List<Double>>> paths;

        public RouteFeatureGeometry() {
            paths = new ArrayList<>();
        }
    }
}
