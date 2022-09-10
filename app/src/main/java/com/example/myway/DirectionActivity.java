package com.example.myway;

import android.content.Intent;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.widget.EditText;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import android.content.Context;
import android.widget.TextView;


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

        //시간에 따른 혼잡도 표시
        String[] tt= intent.getStringArrayExtra("Tlist");//시간 받아오기
        String Timeset3 = tt[0];

        int Timeset4 = Integer.parseInt(Timeset3); //if문 위해 String->int로 타입 변환

        EditText tText =(EditText) findViewById(R.id.timeView2);
        //출퇴근시간 6-8/17-19시 사이면->혼잡 이외면 여유 반환.. 추후 세분화 예정
        if (((Timeset4>=6)&&(Timeset4<=8))||((Timeset4>=17)&&(Timeset4<=19))) {
            tText.setText("혼잡");
        }else{
            tText.setText("여유");
        }


        //출발역 text 지정 추후 Edittext -> TextView로 변경 예정
        String[] ss = intent.getStringArrayExtra("Sstation");
        String Depart_station = ss[2];
        EditText sText = (EditText) findViewById(R.id.start_station_view);
        sText.setText(Depart_station);

        //도착역 text 지정 추후 Edittext -> TextView로 변경 예정
        String[] as = intent.getStringArrayExtra("Astation");
        String Arrival_station = as[2];
        EditText aText = (EditText) findViewById(R.id.arrival_station_view);
        aText.setText(Arrival_station);

        ////////////////////지하철 맵 생성
        HashMap<String, HashSet<String>> transferMap= new HashMap<String, HashSet<String>>();

        String fileName = "mywaydata.txt";

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

            //출발역, 도착역 지정 - toString() 하지 않을시 오류 발생
            String depart = Depart_station.toString();
            String arrival = Arrival_station.toString();

            //출발역, 도착역 노드 지정, 다익스트라 알고리즘 실행
            StationNode departNode=graph.findNode(depart);
            Dijkstra dijsktra = new Dijkstra(graph,departNode,arrival);
            dijsktra.disjkstra();

            //중간 역들 저장 및 "," 기준으로 나누어 sts배열에 저장
            String need_stations = dijsktra.stations;
            String[] sts = need_stations.split(",");

            //ArrayList 형식으로 중복된 역 제거 - 환승역이 두번씩 표기되는 문제 해결
            ArrayList<String> stList = new ArrayList<>();
            for (String item : sts){
                if(!stList.contains(item))
                    stList.add(item);
            }
            stList.set(0, stList.get(0).replace("null",""));
            Collections.reverse(stList);

            //다시 배열 형식으로 바꾸고 맨 첫번째 역(reverse 했기에 맨 마지막 역이었던 것)에 null이 붙는 문제 해결
            //stList[-1] = stList[-1].replace("null","");
            String[] stData = stList.toArray(new String[stList.size()]);


            System.out.println(Arrays.toString(stData));

            EditText stTest = (EditText) findViewById(R.id.stations_view);
            stTest.setText(Arrays.toString(stData));


        } catch (IOException e) {
            System.err.println(e); // 에러가 있다면 메시지 출력
            System.exit(1);
        }

    }
}