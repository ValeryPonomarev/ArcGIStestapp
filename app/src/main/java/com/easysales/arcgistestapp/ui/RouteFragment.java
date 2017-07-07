package com.easysales.arcgistestapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.easysales.arcgistestapp.ApplicationWrapper;
import com.easysales.arcgistestapp.R;
import com.easysales.arcgistestapp.ui.base.BaseMapFragment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.*;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.esri.arcgisruntime.tasks.networkanalysis.Stop;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouteFragment extends BaseMapFragment {
    public static String TAG = "RouteFragment";

    private GraphicsOverlay graphicsOverlay;

    private SimpleMarkerSymbol sourceSymbol;
    private SimpleMarkerSymbol destinationSymbol;
    private SimpleLineSymbol routeSymbol;
    private Point sourcePoint = null;
    private Point destinationPoint = null;
    private final SpatialReference wgs84 = SpatialReference.create(4326);

    @BindView(R.id.map)
    MapView mapView;

    public RouteFragment() {
    }

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);
        ButterKnife.bind(this, view);

        initSymbols();

        Location lastLocation = ApplicationWrapper.getInstance().getLocationHelper().getLastLocation();
        sourcePoint = new Point(lastLocation.getLongitude(), lastLocation.getLatitude(), wgs84);

        initMap();

        return view;
    }

    private void initMap(){

        Basemap basemap = new Basemap(new ArcGISVectorTiledLayer(getResources().getString(R.string.basemap_service)));
        final ArcGISMap map = new ArcGISMap(basemap);

        ArcGISTiledLayer layer = null;


        Viewpoint lastLocationPoint = new Viewpoint(sourcePoint, 200000);
        map.setInitialViewpoint(lastLocationPoint);
        map.loadAsync();
        map.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                mapView.setMap(map);
                graphicsOverlay = new GraphicsOverlay();
                mapView.getGraphicsOverlays().add(graphicsOverlay);
                drawSourcePoint();
                mapView.setOnTouchListener(new RouteMapViewOnTouchListener(getActivity(), mapView));
            }
        });
    }

    @Override
    protected MapView getMapView() {
        return mapView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.route_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.find_route){
            findRoute();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initSymbols() {
        sourceSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.GREEN, 20);
        destinationSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.GRAY, 20);
        routeSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
    }

    private void findRoute(){
        Toast.makeText(getActivity(), "findRoute", Toast.LENGTH_SHORT).show();
        final RouteTask routeTask = new RouteTask(getResources().getString(R.string.local_routing_service));
        final ListenableFuture<RouteParameters> listenableFuture = routeTask.createDefaultParametersAsync();
        listenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!listenableFuture.isDone()){
                        return;
                    }

                    RouteParameters parameters = listenableFuture.get();
                    List<Stop> routeStops = parameters.getStops();
                    parameters.setReturnDirections(true);
                    routeStops.add(new Stop(sourcePoint));
                    routeStops.add(new Stop(destinationPoint));

                    RouteResult result = routeTask.solveRouteAsync(parameters).get();
                    Route route = result.getRoutes().get(0);
                    Graphic graphic = new Graphic(route.getRouteGeometry(), routeSymbol);
                    graphicsOverlay.getGraphics().add(2, graphic);

                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
    }

    private void drawSourcePoint(){
        graphicsOverlay.getGraphics().add(new Graphic(sourcePoint, sourceSymbol));
    }

    private class RouteMapViewOnTouchListener extends DefaultMapViewOnTouchListener{
        public RouteMapViewOnTouchListener(Context context, MapView mapView) {
            super(context, mapView);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            destinationPoint = getMapPoint(e);
            graphicsOverlay.getGraphics().clear();
            drawSourcePoint();
            Graphic graphic = new Graphic(destinationPoint, destinationSymbol);
            graphic.setSymbol(destinationSymbol);
            graphicsOverlay.getGraphics().add(1, graphic);
            return super.onSingleTapConfirmed(e);
        }
    }
}
