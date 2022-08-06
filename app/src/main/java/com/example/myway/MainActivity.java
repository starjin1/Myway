package com.example.myway;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

public class MainActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener{
    RelativeLayout mapViewContainer;
    MapView mapView;
    private View hader;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        initMaoView();

        Button button = (Button) findViewById(R.id.main_linemap);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TrainActivity.class);
                startActivity(intent);

            }
        });
        Button button2  = (Button) findViewById(R.id.main_search);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TrainInfoActivity.class);
                startActivity(intent);
            }
        });

        Button button3 = (Button) findViewById(R.id.main_find_load);
        button3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(getApplicationContext(), ChatbotActivity.class);
                startActivity(intent);
            }
        });

        ImageView calendarImg = (ImageView) findViewById(R.id.main_calendar);
        calendarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),CalendarActivity.class);
                startActivity(intent);
            }
        });

        ImageView userImg = (ImageView) findViewById(R.id.main_personal);
        userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
            }
        });

        ImageView mapAddImg = (ImageView) findViewById(R.id.main_mapBtn);
        mapAddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
                mapViewContainer.removeView(mapView);
            }
        });

    }
//    private void initMaoView() {
//        mapViewContainer = (RelativeLayout) findViewById(R.id.map_view);
//        mapView = new MapView(this);
//        mapViewContainer.addView(mapView);
//        mapView.setCurrentLocationEventListener(this);
//        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
//    }
//
//    @Override
//    protected void onResume() {
//        ConstraintLayout coord = (ConstraintLayout)findViewById(R.id.map_container);
//        super.onResume();
//        if (coord.getChildAt(0)==null){
//            try{
//                initMaoView();
//
//            }catch (RuntimeException re){
//                Log.e("MapActivity","onResume : "+String.valueOf(re));
//            }
//        }
//    }



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