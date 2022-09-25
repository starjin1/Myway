package com.example.myway;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myway.adapters.LocationAdapter;
import com.example.myway.interfaces.ApiInterface;
import com.example.myway.models.ApiClient;
import com.example.myway.models.CategoryResult;
import com.example.myway.models.Document;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.eventbus.Subscribe;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapActivity extends AppCompatActivity implements MapView.MapViewEventListener, MapView.POIItemEventListener,MapView.OpenAPIKeyAuthenticationResultListener,View.OnClickListener,MapView.CurrentLocationEventListener {

    //xml
    final static String TAG = "MapTAG";
    MapView mMapView;
    ViewGroup mMapViewContainer;
    EditText mSearchEdit;
    private Animation fap_open, fap_close;
    private Boolean isFapOpen = false;
    private FloatingActionButton fab, fab1,fab2,stopTrackingFab,clear_cap,clear_maker;
    private ExtendedFloatingActionButton cap1,cap2,cap3,cap4,cap5,cap6,cap7,cap8,cap9;
    RelativeLayout mLoaderLayout;
    RecyclerView recyclerView;

    //value
    MapPoint currentMapPoint;
    private double mCurrentLng;
    private double mCurrentLat;
    private double mSearchLng = -1;
    private double mSearchLat = -1;
    private String mSearchName;
    boolean isTrackingMode = false; //트래킹 모드인지 아닌지 판단

    ArrayList<Document> bigMartList = new ArrayList<>(); //대형마트 MT1
    ArrayList<Document> gs24List = new ArrayList<>(); //편의점 CS2
    ArrayList<Document> schoolList = new ArrayList<>(); //학교 SC4
    ArrayList<Document> academyList = new ArrayList<>(); //학원 AC5
    ArrayList<Document> subwayList = new ArrayList<>(); //지하철 SW8
    ArrayList<Document> bankList = new ArrayList<>(); //은행 BK9
    ArrayList<Document> hospitalList = new ArrayList<>(); //병원 HP8
    ArrayList<Document> pharmacyList = new ArrayList<>(); //약국 PM9
    ArrayList<Document> cafeList = new ArrayList<>(); //카페
    ArrayList<Document> documentArrayList = new ArrayList<>(); //지역명 검색 결과 리스트

    MapPOIItem searchMarker = new MapPOIItem();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.map_layout);
        initView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        getSupportActionBar().setTitle("");
        toolbar.setSubtitle("");

    }

    private void initView() {
        mSearchEdit = findViewById(R.id.map_et_search);
        fap_open = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_open);
        fap_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        fab = findViewById(R.id.fab);
        fab1 = findViewById(R.id.fab1);
        fab2 = findViewById(R.id.fab2);
        cap1 = findViewById(R.id.cap1);
        cap2 = findViewById(R.id.cap2);
        cap3 = findViewById(R.id.cap3);
        cap4 = findViewById(R.id.cap4);
        cap5 = findViewById(R.id.cap5);
        cap6 = findViewById(R.id.cap6);
        cap7 = findViewById(R.id.cap7);
        cap8 = findViewById(R.id.cap8);
        cap9 = findViewById(R.id.cap9);
        clear_cap = findViewById(R.id.clear_cap);
        clear_maker =findViewById(R.id.clear_marker);
        stopTrackingFab = findViewById(R.id.fab_stop_tracking);
        mLoaderLayout = findViewById(R.id.loaderLayout);
        mMapView = new MapView(this);
        mMapViewContainer = findViewById(R.id.map_mv_mapcontainer);
        mMapViewContainer.addView(mMapView);
        recyclerView = findViewById(R.id.map_recyclerview);
        LocationAdapter locationAdapter = new LocationAdapter(documentArrayList,getApplicationContext(),mSearchEdit,recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false); //레이아웃 매니저 세팅
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),DividerItemDecoration.VERTICAL)); // 아래구분선 세팅
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(locationAdapter);

        //맵
        mMapView.setMapViewEventListener(this);
        mMapView.setPOIItemEventListener(this);
        mMapView.setOpenAPIKeyAuthenticationResultListener(this);

        //버튼
        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        stopTrackingFab.setOnClickListener(this);
        clear_cap.setOnClickListener(this);
        clear_maker.setOnClickListener(this);

        Toast.makeText(this, "맵을 로딩중입니다", Toast.LENGTH_SHORT).show();

        // 맵 (현재 위치 없데이트)
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
        mLoaderLayout.setVisibility(View.VISIBLE);

        //EditText 검색 이벤트
        mSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() >= 1) {
                    documentArrayList.clear();
                    locationAdapter.clear();
                    ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
                    Call<CategoryResult> call = apiInterface.getSearchLocation(getString(R.string.restapi_key),charSequence.toString(),15);
                    call.enqueue(new Callback<CategoryResult>() {
                        @Override
                        public void onResponse(Call<CategoryResult> call, Response<CategoryResult> response) {
                            if (response.isSuccessful()) {
                                assert  response.body() != null;
                                for (Document document : response.body().getDocuments()) {
                                    locationAdapter.addItem(document);
                                    search(document);
                                }
                                locationAdapter.notifyDataSetChanged();

                            }
                        }

                        @Override
                        public void onFailure(Call<CategoryResult> call, Throwable t) {

                        }
                    });

                } else {
                    if (charSequence.length() <= 0) {
                        recyclerView.setVisibility(View.GONE);
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mSearchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                } else {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
        mSearchEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "검색 리스트에서 장소를 선택해주세요", Toast.LENGTH_SHORT).show();
            }
        });
        cap4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestMartSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestBankSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestschoolSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestAcademicSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestSubwaySearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestgs24Search(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestHospitalSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestPharmacySearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });

        cap9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                mLoaderLayout.setVisibility(View.VISIBLE);
//                anim();
                clear_maker.setVisibility(View.VISIBLE);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                requestCafeSearch(mCurrentLng,mCurrentLat);
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
            }
        });


    }



    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:
                Toast.makeText(this,
                        "2번 버튼 : 현재위치 기준으로 주변환경 검색 " +
                                "3번 버튼: 현재 위치 추적 및 업데이트", Toast.LENGTH_SHORT).show();
                anim();
                break;

            case R.id.fab1:
                Toast.makeText(this, "현재 위치 추적 시작", Toast.LENGTH_SHORT).show();
                mLoaderLayout.setVisibility(View.VISIBLE); //로딩중입니다 표시
                isTrackingMode = true;
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading);
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                anim();
                stopTrackingFab.setVisibility(View.VISIBLE);
                mLoaderLayout.setVisibility(View.GONE);
                break;

            case R.id.fab2:
                isTrackingMode =false;
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                Toast.makeText(this, "현재 위치기준 1km 검색 시작", Toast.LENGTH_SHORT).show();
//                mLoaderLayout.setVisibility(View.VISIBLE);
                anim();
                cap_open();
                clear_cap.setVisibility(View.VISIBLE);
                break;


            case R.id.fab_stop_tracking:
                isTrackingMode = false;
                mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                stopTrackingFab.setVisibility(View.GONE);
                Toast.makeText(this, "현재위치 추적 종료", Toast.LENGTH_SHORT).show();
                break;

            case R.id.clear_cap:
                isTrackingMode =false;
                cap_close();
                clear_cap.setVisibility(View.GONE);
                Toast.makeText(this, "cap 삭제", Toast.LENGTH_SHORT).show();
                break;

            case R.id.clear_marker:
                isTrackingMode = false;
                mMapView.removeAllPOIItems();
                mMapView.removeAllCircles();
                clear_maker.setVisibility(View.GONE);
                Toast.makeText(this, "maker 삭제", Toast.LENGTH_SHORT).show();

        }

    }



    private void requestMartSearch(double x, double y) {
        bigMartList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "MT1", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "bigMartList Success");
                        bigMartList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", bigMartList.size() + "");

                        int tagNum = 10;
                        for (Document document : bigMartList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestBankSearch(double x, double y) {
        bankList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "BK9", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "bankList Success");
                        bankList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", bankList.size() + "");

                        int tagNum = 10;
                        for (Document document : bankList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestschoolSearch(double x, double y) {
        schoolList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "SC4", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "schoolList Success");
                        schoolList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", schoolList.size() + "");

                        int tagNum = 10;
                        for (Document document : schoolList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestAcademicSearch(double x, double y) {
        academyList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "AC5", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "academyList Success");
                        academyList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", academyList.size() + "");

                        int tagNum = 10;
                        for (Document document : academyList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestSubwaySearch(double x, double y) {
        subwayList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "SW8", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "subwayList Success");
                        subwayList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", subwayList.size() + "");

                        int tagNum = 10;
                        for (Document document : subwayList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestgs24Search(double x, double y) {
        gs24List.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "CS2", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "gs24List Success");
                        gs24List.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", gs24List.size() + "");

                        int tagNum = 10;
                        for (Document document : gs24List) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestHospitalSearch(double x, double y) {
        hospitalList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "HP8", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "hospitalList Success");
                        hospitalList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", hospitalList.size() + "");

                        int tagNum = 10;
                        for (Document document : hospitalList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestCafeSearch(double x, double y) {
        cafeList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "CE7", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "cafeList Success");
                        cafeList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", cafeList.size() + "");

                        int tagNum = 10;
                        for (Document document : cafeList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    private void requestPharmacySearch(double x, double y) {
        pharmacyList.clear();
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "PM9", x + "", y + "", 1000);
        call.enqueue(new Callback<CategoryResult>() {
            @Override
            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    if (response.body().getDocuments() != null) {
                        Log.d(TAG, "pharmacyList Success");
                        pharmacyList.addAll(response.body().getDocuments());

                        MapCircle circle1 = new MapCircle(
                                MapPoint.mapPointWithGeoCoord(y, x), // center
                                1000, // radius
                                Color.argb(128, 255, 0, 0), // strokeColor
                                Color.argb(0, 0, 0, 0) // fillColor
                        );
                        circle1.setTag(5678);
                        mMapView.addCircle(circle1);
                        Log.d("SIZE1", pharmacyList.size() + "");

                        int tagNum = 10;
                        for (Document document : pharmacyList) {
                            MapPOIItem marker = new MapPOIItem();
                            marker.setItemName(document.getPlaceName());
                            marker.setTag(tagNum++);
                            double x = Double.parseDouble(document.getY());
                            double y = Double.parseDouble(document.getX());
                            //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
                            MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
                            marker.setMapPoint(mapPoint);
                            marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
                            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
                            mMapView.addPOIItem(marker);
                        }
                    }

                }
            }

            @Override
            public void onFailure(Call<CategoryResult> call, Throwable t) {

            }
        });
    }

    public void cap_open() {
        cap1.setVisibility(View.VISIBLE);
        cap2.setVisibility(View.VISIBLE);
        cap3.setVisibility(View.VISIBLE);
        cap4.setVisibility(View.VISIBLE);
        cap5.setVisibility(View.VISIBLE);
        cap6.setVisibility(View.VISIBLE);
        cap7.setVisibility(View.VISIBLE);
        cap8.setVisibility(View.VISIBLE);
        cap9.setVisibility(View.VISIBLE);
    }

    public void cap_close() {
        cap1.setVisibility(View.GONE);
        cap2.setVisibility(View.GONE);
        cap3.setVisibility(View.GONE);
        cap4.setVisibility(View.GONE);
        cap5.setVisibility(View.GONE);
        cap6.setVisibility(View.GONE);
        cap7.setVisibility(View.GONE);
        cap8.setVisibility(View.GONE);
        cap9.setVisibility(View.GONE);
    }

//    private void requestSearchLocal(double x, double y) {
//        bigMartList.clear();
//        gs24List.clear();
//        schoolList.clear();
//        academyList.clear();
//        subwayList.clear();
//        bankList.clear();
//        hospitalList.clear();
//        pharmacyList.clear();
//        cafeList.clear();
//        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);
//        Call<CategoryResult> call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "MT1", x + "", y + "", 1000);
//        call.enqueue(new Callback<CategoryResult>() {
//            @Override
//            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                if (response.isSuccessful()) {
//                    assert response.body() != null;
//                    if (response.body().getDocuments() != null) {
//                        Log.d(TAG, "bigMartList Success");
//                        bigMartList.addAll(response.body().getDocuments());
//                    }
//                    call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "CS2", x + "", y + "", 1000);
//                    call.enqueue(new Callback<CategoryResult>() {
//                        @Override
//                        public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                            if (response.isSuccessful()) {
//                                assert response.body() != null;
//                                Log.d(TAG, "gs24List Success");
//                                gs24List.addAll(response.body().getDocuments());
//                                call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "SC4", x + "", y + "", 1000);
//                                call.enqueue(new Callback<CategoryResult>() {
//                                    @Override
//                                    public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                        if (response.isSuccessful()) {
//                                            assert response.body() != null;
//                                            Log.d(TAG, "schoolList Success");
//                                            schoolList.addAll(response.body().getDocuments());
//                                            call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "AC5", x + "", y + "", 1000);
//                                            call.enqueue(new Callback<CategoryResult>() {
//                                                @Override
//                                                public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                    if (response.isSuccessful()) {
//                                                        assert response.body() != null;
//                                                        Log.d(TAG, "academyList Success");
//                                                        academyList.addAll(response.body().getDocuments());
//                                                        call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "SW8", x + "", y + "", 1000);
//                                                        call.enqueue(new Callback<CategoryResult>() {
//                                                            @Override
//                                                            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                                if (response.isSuccessful()) {
//                                                                    assert response.body() != null;
//                                                                    Log.d(TAG, "subwayList Success");
//                                                                    subwayList.addAll(response.body().getDocuments());
//                                                                    call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "BK9", x + "", y + "", 1000);
//                                                                    call.enqueue(new Callback<CategoryResult>() {
//                                                                        @Override
//                                                                        public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                                            if (response.isSuccessful()) {
//                                                                                assert response.body() != null;
//                                                                                Log.d(TAG, "bankList Success");
//                                                                                bankList.addAll(response.body().getDocuments());
//                                                                                call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "HP8", x + "", y + "", 1000);
//                                                                                call.enqueue(new Callback<CategoryResult>() {
//                                                                                    @Override
//                                                                                    public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                                                        if (response.isSuccessful()) {
//                                                                                            assert response.body() != null;
//                                                                                            Log.d(TAG, "hospitalList Success");
//                                                                                            hospitalList.addAll(response.body().getDocuments());
//                                                                                            call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "PM9", x + "", y + "", 1000);
//                                                                                            call.enqueue(new Callback<CategoryResult>() {
//                                                                                                @Override
//                                                                                                public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                                                                    if (response.isSuccessful()) {
//                                                                                                        assert response.body() != null;
//                                                                                                        Log.d(TAG, "pharmacyList Success");
//                                                                                                        pharmacyList.addAll(response.body().getDocuments());
//                                                                                                        call = apiInterface.getSearchCategory(getString(R.string.restapi_key), "CE7", x + "", y + "", 1000);
//                                                                                                        call.enqueue(new Callback<CategoryResult>() {
//                                                                                                            @Override
//                                                                                                            public void onResponse(@NotNull Call<CategoryResult> call, @NotNull Response<CategoryResult> response) {
//                                                                                                                if (response.isSuccessful()) {
//                                                                                                                    assert response.body() != null;
//                                                                                                                    Log.d(TAG, "cafeList Success");
//                                                                                                                    cafeList.addAll(response.body().getDocuments());
//                                                                                                                    //모두 통신 성공 시 circle 생성
//                                                                                                                    MapCircle circle1 = new MapCircle(
//                                                                                                                            MapPoint.mapPointWithGeoCoord(y, x), // center
//                                                                                                                            1000, // radius
//                                                                                                                            Color.argb(128, 255, 0, 0), // strokeColor
//                                                                                                                            Color.argb(0, 0, 0, 0) // fillColor
//                                                                                                                    );
//                                                                                                                    circle1.setTag(5678);
//                                                                                                                    mMapView.addCircle(circle1);
//                                                                                                                    Log.d("SIZE1", bigMartList.size() + "");
//                                                                                                                    Log.d("SIZE2", gs24List.size() + "");
//                                                                                                                    Log.d("SIZE3", schoolList.size() + "");
//                                                                                                                    Log.d("SIZE4", academyList.size() + "");
//                                                                                                                    Log.d("SIZE5", subwayList.size() + "");
//                                                                                                                    Log.d("SIZE6", bankList.size() + "");
//                                                                                                                    //마커 생성
//                                                                                                                    int tagNum = 10;
//                                                                                                                    for (Document document : bigMartList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        marker.setCustomImageAutoscale(false); // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.
//                                                                                                                        marker.setCustomImageAnchor(0.5f, 1.0f); // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : gs24List) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : schoolList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : academyList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : subwayList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : bankList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : hospitalList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                    }
//                                                                                                                    for (Document document : pharmacyList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                        mLoaderLayout.setVisibility(View.GONE);
//
//                                                                                                                    }
//                                                                                                                    for (Document document : cafeList) {
//                                                                                                                        MapPOIItem marker = new MapPOIItem();
//                                                                                                                        marker.setItemName(document.getPlaceName());
//                                                                                                                        marker.setTag(tagNum++);
//                                                                                                                        double x = Double.parseDouble(document.getY());
//                                                                                                                        double y = Double.parseDouble(document.getX());
//                                                                                                                        //카카오맵은 참고로 new MapPoint()로  생성못함. 좌표기준이 여러개라 이렇게 메소드로 생성해야함
//                                                                                                                        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(x, y);
//                                                                                                                        marker.setMapPoint(mapPoint);
//                                                                                                                        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 마커타입을 기본 마커로 지정.
//                                                                                                                        mMapView.addPOIItem(marker);
//                                                                                                                        mLoaderLayout.setVisibility(View.GONE);
//                                                                                                                    }
//                                                                                                                }
//                                                                                                            }
//
//                                                                                                            @Override
//                                                                                                            public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                                                                                            }
//                                                                                                        });
//                                                                                                    }
//                                                                                                }
//
//                                                                                                @Override
//                                                                                                public void onFailure(@NotNull Call<CategoryResult> call, Throwable t) {
//
//                                                                                                }
//                                                                                            });
//                                                                                        }
//                                                                                    }
//
//                                                                                    @Override
//                                                                                    public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                                                                    }
//                                                                                });
//                                                                            }
//                                                                        }
//
//                                                                        @Override
//                                                                        public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                                                        }
//                                                                    });
//                                                                }
//                                                            }
//
//                                                            @Override
//                                                            public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                                            }
//                                                        });
//                                                    }
//                                                }
//
//                                                @Override
//                                                public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                                }
//                                            });
//                                        }
//                                    }
//
//                                    @Override
//                                    public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                                    }
//                                });
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//
//                        }
//                    });
//                }
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<CategoryResult> call, @NotNull Throwable t) {
//                Log.d(TAG, "FAIL");
//            }
//        });
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                super.onBackPressed();
                finish();
                startActivity(new Intent(MapActivity.this,MainActivity.class));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void finish() {
        mMapViewContainer.removeView(mMapView);
        super.finish();
    }



    public void anim() {
        if (isFapOpen) {
            fab1.startAnimation(fap_close);
            fab2.startAnimation(fap_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            isFapOpen = false;
        } else {
            fab1.startAnimation(fap_open);
            fab2.startAnimation(fap_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            isFapOpen = true;
        }
    }

    @Subscribe //검색예시 클릭시 이벤트 오토버스
    public void search(Document document) {//public항상 붙여줘야함
        mSearchName = document.getPlaceName();
        mSearchLng = Double.parseDouble(document.getX());
        mSearchLat = Double.parseDouble(document.getY());
        mMapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng), true);
        mMapView.removePOIItem(searchMarker);
        searchMarker.setItemName(mSearchName);
        searchMarker.setTag(10000);
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(mSearchLat, mSearchLng);
        searchMarker.setMapPoint(mapPoint);
        searchMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        searchMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mMapView.addPOIItem(searchMarker);
    }


    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, v));
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        //이 좌표로 지도 중심 이동
        mMapView.setMapCenterPoint(currentMapPoint, true);
        //전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        Log.d(TAG, "현재위치 => " + mCurrentLat + "  " + mCurrentLng);
        mLoaderLayout.setVisibility(View.GONE);
        if (!isTrackingMode) {
            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        }

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
        Log.i(TAG, "onCurrentLocationUpdateFailed");
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

    }



    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
        Log.i(TAG, "onCurrentLocationUpdateFailed");
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        recyclerView.setVisibility(View.GONE);

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onDaumMapOpenAPIKeyAuthenticationResult(MapView mapView, int i, String s) {

    }

    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}


