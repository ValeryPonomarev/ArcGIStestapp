package com.easysales.arcgistestapp.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by lordp on 23.06.2017.
 */

public class LocationHelper {

    private static final double MOSCOW_LATITUDE = 37.759121;//32.7157;//55.75176486026602;
    private static final double MOSCOW_LONGITUDE = -122.389542;//-117.1611;//37.61069118976593;
    private static Location DEFAULT_LOCATION = new Location("san diego");

    static {
        DEFAULT_LOCATION.setLongitude(MOSCOW_LONGITUDE);
        DEFAULT_LOCATION.setLatitude(MOSCOW_LATITUDE);
    }

    public Location getLastLocation() {
        return DEFAULT_LOCATION;
    }
}
