package com.example.myway;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends AppCompatActivity {
    WebView main_map_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_map_view = findViewById(R.id.main_map_view);
        WebSettings webSettings = main_map_view.getSettings();
        webSettings.setJavaScriptEnabled(true); // allow the js

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //화면이 계속 켜짐
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);

        main_map_view.setWebViewClient(new WebViewClient());
        main_map_view.loadUrl("https://www.google.co.kr/maps/");

//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide(); // 액션바 숨기기
    }
}