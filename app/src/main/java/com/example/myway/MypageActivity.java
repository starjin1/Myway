package com.example.myway;

import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;



public class MypageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mypage_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toobar_mypage);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_left);
        getSupportActionBar().setTitle("");
        toolbar.setSubtitle("");
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

