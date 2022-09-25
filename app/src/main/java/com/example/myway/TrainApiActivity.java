package com.example.myway;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

public class TrainApiActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_api);



    }




//    public void tOnClick(View v){
//        switch (v.getId()){
//            case R.id.TrainButton:
//
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        data = getXmlData();
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                text.setText(data);
//                            }
//                        });
//                    }
//                }).start();
//
//                break;
//        }
//    }
//
//    String getXmlData(){
//        StringBuffer buffer = new StringBuffer();
//
//        String str = edit.getText().toString();
//        String location = URLEncoder.encode(str);
//
//        StringBuilder urlBuilder = new StringBuilder("http://swopenAPI.seoul.go.kr/api/subway/");
//
//        String queryUrl = "http://swopenapi.seoul.go.kr/api/subway/6259474d5573616c3131335862467179/xml/realtimeStationArrival/1/5/%EB%A9%B4%EB%AA%A9";
//
//        try{
//            URL url = new URL(queryUrl);
//            InputStream is = url.openStream();
//
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            XmlPullParser xpp = factory.newPullParser();
//            xpp.setInput(new InputStreamReader(is, "UTF-8"));
//
//            String tag;
//
//            xpp.next();
//            int eventType = xpp.getEventType();
//
//            while (eventType != XmlPullParser.END_DOCUMENT){
//                switch (eventType){
//                    case XmlPullParser.START_DOCUMENT:
//                        buffer.append("파싱시작\n\n");
//                        break;
//
//                    case XmlPullParser.START_TAG:
//                        tag = xpp.getName();
//
//                        if(tag.equals("item"));
//                        else if(tag.equals("updnLine")){
//                            buffer.append("상행 하행 여부:");
//                            xpp.next();
//                            buffer.append(xpp.getText());
//                            buffer.append("\n");
//                        }
//                }
//            }
//        }catch(Exception e){
//
//        }buffer.append("파싱 끝\n");
//        return buffer.toString();
//    }


}