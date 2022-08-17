package com.example.myway;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import android.content.Context;


import androidx.appcompat.app.AppCompatActivity;

public class DirectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        try {
            Thread.sleep(1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_direction);

        String[] ss = intent.getStringArrayExtra("Sstation");
        String sss = ss[2];
        EditText sText = (EditText) findViewById(R.id.start_station_view);
        sText.setText(ss[2]);

        String[] as = intent.getStringArrayExtra("Astation");
        String ass = as[2];
        EditText aText = (EditText) findViewById(R.id.arrival_station_view);
        aText.setText(as[2]);

//        String[] ss = ((ChatbotActivity)ChatbotActivity.context).startArr;
//        String sss = ss[2];
//        EditText sText = (EditText) findViewById(R.id.start_station_view);
//        sText.setText(ss[2]);
//
//        String[] as = ((ChatbotActivity)ChatbotActivity.context).arrivalArr;
//        EditText aText = (EditText) findViewById(R.id.arrival_station_view);
//        aText.setText(as[2]);
//        String ass = as[2];

        ////////////////////지하철 맵 생성
        HashMap<String, HashSet<String>> transferMap= new HashMap<String, HashSet<String>>();

        String fileName = "data.txt";

        int c=0;
        try {
            ////////////////////////////////////////////////////////////////
            InputStream is = getResources().openRawResource(R.raw.data);

            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            String s;
            boolean edgeOrVertex = true;

            ListGraph graph= new ListGraph();
            while ((s = in.readLine()) != null) {
                if(s.equals("")){//start to save edge information.
                    edgeOrVertex = false;
                    continue;
                }
                if(edgeOrVertex){
                    String token[];
                    token = s.split(" ");//add Vertex
                    graph.addNode(token[0], token[1], token[2]);

                    if(transferMap.containsKey(token[1])){
                        transferMap.get(token[1]).add(token[0]);
                    }
                    else{
                        HashSet<String> set= new HashSet<String>();
                        set.add(token[0]);
                        transferMap.put(token[1],set);
                    }
                }
                else{
                    String token[];
                    token = s.split(" ");//add Edge
                    int t = Integer.parseInt(token[2]);
                    graph.addEdge(token[0], token[1], t);
                }
            }
            in.close();
            is.close();

            Iterator<Entry<String, HashSet<String>>> it = transferMap.entrySet().iterator();
            while (it.hasNext()) {
                HashSet<String> aSet = (HashSet<String>)it.next().getValue();
                if(aSet.size()>1){
                    Iterator<String> iterator = aSet.iterator();
                    while(iterator.hasNext()){
                        String aNum = (String) iterator.next();
                        Iterator<String> iterator2 = aSet.iterator();
                        while(iterator2.hasNext()){
                            String anotherNum = (String) iterator2.next();
                            if (aNum.equals(anotherNum))
                                continue;
                            graph.addEdge(aNum,anotherNum,5);
                        }
                    }
                }
            }

            ////////////////////////////////////////////////////////////////
            String depart = sss.toString();
            String arrival = ass.toString();

            StationNode departNode=graph.findNode(depart);
            Dijkstra dijsktra = new Dijkstra(graph,departNode,arrival);
            dijsktra.disjkstra();

        } catch (IOException e) {
            System.err.println(e); // 에러가 있다면 메시지 출력
            System.exit(1);
        }

    }
}