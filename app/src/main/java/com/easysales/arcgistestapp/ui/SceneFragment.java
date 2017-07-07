package com.easysales.arcgistestapp.ui;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArraySet;
import android.telephony.gsm.GsmCellLocation;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.easysales.arcgistestapp.ApplicationWrapper;
import com.easysales.arcgistestapp.R;
import com.easysales.arcgistestapp.api.ArcGISScene.ApiClient;
import com.easysales.arcgistestapp.api.ArcGISScene.ApiServiceHolder;
import com.easysales.arcgistestapp.api.net.RouteResolveRequest;
import com.easysales.arcgistestapp.api.net.RouteResolveResponse;
import com.easysales.arcgistestapp.ui.base.BaseFragment;
import com.easysales.arcgistestapp.utils.GenericAsyncTask;
import com.easysales.arcgistestapp.utils.RunnableWithParam;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.io.ArcGISDownloadRequest;
import com.esri.arcgisruntime.layers.ArcGISSceneLayer;
import com.esri.arcgisruntime.layers.Layer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.LayerSceneProperties;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.security.Credential;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.networkanalysis.Route;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteParameters;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteResult;
import com.esri.arcgisruntime.tasks.networkanalysis.RouteTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SceneFragment extends BaseFragment {

    public static final String TAG = "SceneFragment";

    @BindView(R.id.sceneView)
    public SceneView sceneView;

    @BindView(R.id.floorOneBtn)
    public Button floorOneBtn;

    @BindView(R.id.floorTwoBtn)
    public Button floorTwoBtn;

    @BindView(R.id.floorThreeBtn)
    public Button floorThreeBtn;

    @BindView(R.id.floorAllBtn)
    public Button floorAllBtn;

    private ArcGISScene scene;
    private GraphicsOverlay graphicsOverlay;
    private SimpleMarkerSymbol markerSymbol;
    private SpatialReference spatialReference;

    private List<ArcGISSceneLayer> firstFooorLayers;
    private List<ArcGISSceneLayer> secondFooorLayers;
    private List<ArcGISSceneLayer> thirdFooorLayers;
    private List<ArcGISSceneLayer> mainLayers;

    private Point startRoutePoint;
    private Point endRoutePoint;
    private SimpleLineSymbol routeSymbol;

    private ApiClient.ApiClientCallback apiClientCallback;
    private Handler handler = new Handler();

    public SceneFragment() {
        // Required empty public constructor
    }

    public static SceneFragment newInstance() {
        SceneFragment fragment = new SceneFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scene, container, false);
        ButterKnife.bind(this, view);
        init();
        initLayers();
        drawBuildingAsync(ViewType.ALL);
        setCamera();

        sceneView.setOnTouchListener(new SceneOnTouchListener(sceneView));

        addMarker(startRoutePoint);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        ApplicationWrapper.getInstance().getSceneApiClient().detachCallback(apiClientCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        ApplicationWrapper.getInstance().getSceneApiClient().attachCallback(apiClientCallback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.scene_fragment_menu, menu);
    }
    @OnClick({R.id.floorAllBtn, R.id.floorOneBtn, R.id.floorTwoBtn, R.id.floorThreeBtn})
    public void onChangeFloorClick(View view){
        switch (view.getId()){
            case R.id.floorAllBtn:
                drawBuildingAsync(ViewType.ALL);
                break;
            case R.id.floorOneBtn:
                drawBuildingAsync(ViewType.FIRST);
                break;
            case R.id.floorTwoBtn:
                drawBuildingAsync(ViewType.SECOND);
                break;
            case R.id.floorThreeBtn:
                drawBuildingAsync(ViewType.THIRD);
                break;
        }
    }

    protected void getScenePoint(MotionEvent motionEvent, final OnGetPointListener callback){
        android.graphics.Point screenPoint = new android.graphics.Point(Math.round(motionEvent.getX()), Math.round(motionEvent.getY()));
        final ListenableFuture<Point> listenableFuture = sceneView.screenToLocationAsync(screenPoint);

        listenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                if(callback != null){
                    try {
                        callback.onGetPoint(listenableFuture.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void init() {
        spatialReference = SpatialReferences.getWgs84();

        scene = new ArcGISScene();
        scene.setBasemap(Basemap.createTopographic());

        sceneView.setScene(scene);
        graphicsOverlay = new GraphicsOverlay();
        graphicsOverlay.getSceneProperties().setSurfacePlacement(LayerSceneProperties.SurfacePlacement.RELATIVE);
        sceneView.getGraphicsOverlays().add(graphicsOverlay);

        markerSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.TRIANGLE, Color.BLUE, 10);
        routeSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.BLUE, 5);
        startRoutePoint = new Point(-117.19695045410447, 34.05620650783454, 0.6, spatialReference);

        apiClientCallback = new SceneApiCallback();
    }

    private void initLayers(){
        mainLayers = new ArrayList<>();
        firstFooorLayers = new ArrayList<>();
        secondFooorLayers = new ArrayList<>();
        thirdFooorLayers = new ArrayList<>();

        //mainLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/Trees_Analytical/SceneServer/layers/0"));
        mainLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/Building_Wireframe_OOA/SceneServer/layers/0"));
        //mainLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/Building_Textured_OOA_simple/SceneServer/layers/0"));

        firstFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingInteriorSpace_white_OOA1/SceneServer/layers/0"));
        firstFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Walls_OOA1/SceneServer/layers/0"));
        firstFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Doors_OOA1/SceneServer/layers/0"));

        secondFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingInteriorSpace_white_OOA2/SceneServer/layers/0"));
        secondFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Walls_OOA2/SceneServer/layers/0"));
        secondFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Doors_OOA2/SceneServer/layers/0"));

        thirdFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingInteriorSpace_white_OOA3/SceneServer/layers/0"));
        thirdFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Walls_OOA3/SceneServer/layers/0"));
        thirdFooorLayers.add(new ArcGISSceneLayer("http://tiles.arcgis.com/tiles/z2tnIkrLQ2BRzr6P/arcgis/rest/services/BuildingFloorplanLine_Doors_OOA3/SceneServer/layers/0"));
    }

    private void setCamera(){
        Camera camera = new Camera(34.056270, -117.196433, 50, 270, 70, 0.0);
        sceneView.setViewpointCamera(camera);
    }


    private void drawBuildingAsync(final ViewType viewType)
    {
        floorOneBtn.setEnabled(false);
        floorTwoBtn.setEnabled(false);
        floorThreeBtn.setEnabled(false);
        floorAllBtn.setEnabled(false);

        new GenericAsyncTask<ViewType>(
                new RunnableWithParam<ViewType>() {
                    @Override
                    public void run(ViewType param) {
                        drawBuilding(viewType);
                    }
                },
                new Callable<ViewType>() {
                    @Override
                    public ViewType call() throws Exception {
                        return viewType;
                    }
                }
        ).execute();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                floorOneBtn.setEnabled(true);
                floorTwoBtn.setEnabled(true);
                floorThreeBtn.setEnabled(true);
                floorAllBtn.setEnabled(true);
            }
        }, 1000);
    }

    private void drawBuilding(ViewType viewType){
        scene.getBaseSurface().getElevationSources().clear();
        scene.getOperationalLayers().clear();

        switch (viewType){
            case ALL:
                scene.getOperationalLayers().addAll(firstFooorLayers);
                scene.getOperationalLayers().addAll(secondFooorLayers);
                scene.getOperationalLayers().addAll(thirdFooorLayers);
                break;
            case FIRST:
                scene.getOperationalLayers().addAll(firstFooorLayers);
                break;
            case SECOND:
                scene.getOperationalLayers().addAll(secondFooorLayers);
                break;
            case THIRD:
                scene.getOperationalLayers().addAll(thirdFooorLayers);
                break;
            case OTHER:
                ArcGISTiledElevationSource elevationSource = new ArcGISTiledElevationSource("http://elevation3d.arcgis.com/arcgis/rest/services/WorldElevation3D/Terrain3D/ImageServer");
                scene.getBaseSurface().getElevationSources().add(elevationSource);
                break;
        }

        scene.getOperationalLayers().addAll(mainLayers);
    }

    private void addMarker(Point point){
        Graphic graphic = new Graphic(point, markerSymbol);
        graphicsOverlay.getGraphics().add(graphic);
    }

    private void findRouteRequest(){
        List<Point> routePoints = new ArrayList<>();
        routePoints.add(this.startRoutePoint);
        routePoints.add(this.endRoutePoint);

        //   "{\"spatialReference\":{\"wkid\":102100,\"latestWkid\":3857},\"geometryType\":\"esriGeometryPoint\",\"features\":[{\"geometry\":{\"x\":-13046355.100769324,\"y\":4036360.7802603063,\"z\":0,\"spatialReference\":{\"wkid\":102100,\"latestWkid\":3857}},\"attributes\":{}},{\"geometry\":{\"x\":-13046354.185844446,\"y\":4036381.193904421,\"z\":4.25,\"spatialReference\":{\"wkid\":102100,\"latestWkid\":3857}},\"attributes\":{}}],\"exceededTransferLimit\":null}";
        RouteResolveRequest resolveRequest = new RouteResolveRequest(routePoints);
        ApplicationWrapper.getInstance().getSceneApiClient().routeResolve(resolveRequest, apiClientCallback);
    }

    private void findRoute(){
        try {

            final RouteTask routeTask = new RouteTask(this.getContext(), getResources().getString(R.string.scene_routing_service));

            final RouteParameters routeParameters = routeTask.createDefaultParametersAsync().get();
            routeParameters.setReturnRoutes(true);
            routeParameters.setReturnDirections(false);
            routeParameters.setReturnStops(false);
            routeParameters.setReturnPointBarriers(false);
            routeParameters.setReturnPolygonBarriers(false);
            routeParameters.setReturnPolylineBarriers(false);
            routeParameters.setOutputSpatialReference(spatialReference);

            routeTask.loadAsync();
            routeTask.addDoneLoadingListener(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (routeTask.getLoadError() == null && routeTask.getLoadStatus() == LoadStatus.LOADED) {
                            RouteResult routeResult = routeTask.solveRouteAsync(routeParameters).get();
                            Route route = routeResult.getRoutes().get(0);
                            Graphic graphic = new Graphic(route.getRouteGeometry(), routeSymbol);
                            graphicsOverlay.getGraphics().add(graphic);
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public enum  ViewType {
        ALL, FIRST, SECOND, THIRD, OTHER
    }

    public interface OnGetPointListener {
        void onGetPoint(Point point);
    }

    private class SceneOnTouchListener extends DefaultSceneViewOnTouchListener {
        public SceneOnTouchListener(SceneView sceneView) {
            super(sceneView);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            getScenePoint(motionEvent, new OnGetPointListener() {
                @Override
                public void onGetPoint(Point point) {
                    graphicsOverlay.getGraphics().clear();
                    endRoutePoint = point;
                    addMarker(startRoutePoint);
                    addMarker(endRoutePoint);

                    findRouteRequest();
                }
            });

            return super.onSingleTapUp(motionEvent);
        }
    }

    private class SceneApiCallback implements ApiClient.ApiClientCallback {

        @Override
        public void onRouteResolve(RouteResolveResponse routeResolveResponse, int code) {
            if(code == 200) {
                if (routeResolveResponse == null) return;
                if (routeResolveResponse.routes == null) return;
                if (routeResolveResponse.routes.features == null) { return; }

                RouteResolveResponse.RouteFeatureGeometry featureGeometry = null;
                if(routeResolveResponse.routes.features.size() > 0) {
                    featureGeometry = routeResolveResponse.routes.features.get(0).geometry;
                }

                if(featureGeometry == null) { return; }
                if(featureGeometry.paths.size() == 0) { return; }
                List<List<Double>> pointsData = featureGeometry.paths.get(0);
                List<Point> points = new ArrayList<>();

                for(List<Double> pointData : pointsData){
                    points.add(new Point(pointData.get(0), pointData.get(1), pointData.get(2), spatialReference));
                }

                Polyline routeLine = new Polyline(new PointCollection(points));
                Graphic graphic = new Graphic(routeLine, routeSymbol);
                graphicsOverlay.getGraphics().add(graphic);
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }
    }

    private class DrawBuildingAsyncTask extends AsyncTask<ViewType, Void, Void> {

        @Override
        protected Void doInBackground(ViewType... viewTypes) {
            drawBuilding(viewTypes[0]);
            return null;
        }
    }
}
