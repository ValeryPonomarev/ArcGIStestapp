package com.easysales.arcgistestapp.api.net;

import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by lordp on 06.07.2017.
 */

public class RouteResolveRequest {
    public String f = "json";
    public Boolean returnDirections=false;
    public Boolean returnRoutes=true;
    public Boolean returnZ=true;
    public Boolean returnStops=false;
    public Boolean returnBarriers=false;
    public Boolean returnPolygonBarriers=false;
    public Boolean returnPolylineBarriers=false;
    public Integer outSR=4326;
    public String outputLines = "esriNAOutputLineTrueShape";
    public String restrictionAttributeNames = "Prohibit: Elevators";
    public Stops stops;

    public RouteResolveRequest(List<Point> points) {
        this.stops = new Stops(points);
    }

    public class Stops {
        public SpatialReference spatialReference;
        public String geometryType;
        public List<StopGeometry> features;
        public Object exceededTransferLimit;

        public Stops() {
            spatialReference = new SpatialReference();
            geometryType = "esriGeometryPoint";
            features = new ArrayList<>();
            exceededTransferLimit = null;
        }

        public Stops(List<Point> points) {
            this();
            for(Point point : points){
                features.add(new StopGeometry(point));
            }
        }

        @Override
        public String toString() {
            return new Gson().toJson(this);
        }
    }

    public class SpatialReference {
        public int wkid = 4326;
        public int latestWkid = 4326;
    }

    public class StopGeometry {
        public StopGeometryData geometry;
        public Object attributes;

        public StopGeometry(Point point){
            geometry = new StopGeometryData(point);
            attributes = new Object();
        }
    }

    public class StopGeometryData {
        public Double x;
        public Double y;
        public Double z;
        public SpatialReference spatialReference;

        public StopGeometryData(Point point){
            x = point.getX();
            y = point.getY();
            z = point.getZ();
            spatialReference = new SpatialReference();
        }
    }
}
