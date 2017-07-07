package com.easysales.arcgistestapp.ui.base;

import android.view.MotionEvent;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.view.MapView;

/**
 * Created by lordp on 22.06.2017.
 */

public abstract class BaseMapFragment extends BaseFragment {

    protected abstract MapView getMapView();

    protected Point getMapPoint(MotionEvent motionEvent){
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
        return getMapView().screenToLocation(screenPoint);
    }

    @Override
    public void onPause() {
        super.onPause();
        getMapView().pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        getMapView().resume();
    }
}
