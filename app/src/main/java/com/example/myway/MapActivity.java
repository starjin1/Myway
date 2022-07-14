package com.example.myway;
import android.Manifest;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;


public class MapActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener {
    MapView mapView;
    private static final String LOG_TAG = "MapActivity";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};
    RelativeLayout mapViewContainer;
    boolean isTrackingMode = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        getSupportActionBar().setTitle("");
        toolbar.setSubtitle("");

        mapViewContainer = (RelativeLayout) findViewById(R.id.map_view);
        mapView = new MapView(this);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationEventListener(this);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
    MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
    Log.d("위치 업데이트 ",String.format("업데이트 됨(%f,%f)",mapPointGeo.latitude,mapPointGeo.longitude));
    MapPoint currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude,mapPointGeo.longitude);
    mapView.setMapCenterPoint(currentMapPoint, true);
    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
}

