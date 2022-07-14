package com.example.myway;
import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.google.android.material.navigation.NavigationView;
import com.ortiz.touchview.TouchImageView;


public class TrainActivity extends AppCompatActivity {
    ImageView imageView;
    PhotoViewAttacher mAttacher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subaway_road);
        TouchImageView img = (TouchImageView) findViewById(R.id.imageView2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar_mypage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        getSupportActionBar().setTitle("");
        toolbar.setSubtitle("");

        NavigationView navigationView = findViewById(R.id.navigationView);


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}