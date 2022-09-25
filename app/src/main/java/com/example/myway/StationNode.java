package com.example.myway;

public class StationNode {

    private String number;
    private String stationname;
    private String linenum;
    private int time;
    private int vertex;
    private StationNode prev;



    public StationNode(String n, String s, String l, int c){
        number= n;
        stationname=s;
        linenum=l;
        vertex=c;
        prev=null;
    }

    public StationNode(String n,String s,int v, int t){
        number=n;
        stationname=s;
        time=t;
        vertex = v;
        prev=null;
    }
    public void SetPrev(StationNode station){
        prev=station;
    }

    public StationNode getPrev(){
        return prev;
    }

    public String getNumber(){
        return number;
    }
    public String getStation(){
        return stationname;
    }
    public int getInterval(){
        return time;
    }
    public int getVertex(){
        return vertex;
    }
}
