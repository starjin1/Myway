package com.example.myway;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DirectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_direction);

        String[] ss = ((ChatbotActivity)ChatbotActivity.context).startArr;
        EditText sText = (EditText) findViewById(R.id.start_station_view);
        sText.setText(ss[2]);

        String[] as = ((ChatbotActivity)ChatbotActivity.context).arrivalArr;
        EditText aText = (EditText) findViewById(R.id.arrival_station_view);
        aText.setText(as[2]);


    }
}