package com.example.myway;

import java.util.LinkedList;
import java.util.Iterator;


public class ListGraph {
    private LinkedList<LinkedList<StationNode>> edgeList;
    private int numOfNodes;
    public ListGraph(){
        edgeList = new LinkedList<LinkedList<StationNode>>();
        numOfNodes = 0;
    }
    //	public int getNumOfNodes(){
    //	return numOfNodes;
//	}
//	public LinkedList<LinkedList<StationNode>> getEdgeList(){
//		return edgeList;
//	}
    public void addNode(String n, String s, String l){
        LinkedList<StationNode> newNodeList=new LinkedList<StationNode>();
        StationNode station= new StationNode(n,s,l,numOfNodes);//firstnode starts with vertex 0
        newNodeList.add(station);
        edgeList.add(newNodeList);
        numOfNodes++;
    }
    public void addEdge(String a, String b, int t){
        int vertex = 0;
        String sname=null;
        for(LinkedList<StationNode> element: edgeList){//finding the vertex number of 2nd station
            if(element.get(0).getNumber().equals(b)){
                vertex=element.get(0).getVertex();
                sname= element.get(0).getStation();
            }
        }

        for(LinkedList<StationNode> element: edgeList){
            if(element.get(0).getNumber().equals(a)){
                StationNode edge= new StationNode(b,sname,vertex,t);
                element.add(edge);//addEdge
            }
        }
    }

    @SuppressWarnings("null")
    public LinkedList<StationNode> getEdges (StationNode s){
        //int edgearray[] = null;
        for(LinkedList<StationNode> element: edgeList){
            if(element.get(0).getNumber().equals(s.getNumber())){//if you find the station
                return element;
                //for(int i=1; i<element.size(); i++)//iterate all the edges
                //edgearray[i-1]=element.get(i).getVertex();//save the edges'vertex in the array
            }
        }
        return null;
    }
    public LinkedList<StationNode> getEdges(int vertex){
        for(LinkedList<StationNode> element: edgeList){
            if(element.get(0).getVertex()==vertex){//if you find the station
                return element;
            }
        }
        return edgeList.element();

    }
    public StationNode findNode(int vertex){
        for(LinkedList<StationNode> element: edgeList){
            if(element.get(0).getVertex()==vertex){//if you find the station
                return element.get(0);
            }
        }
        return null;
    }

    public StationNode findNode(String depart){
        for(LinkedList<StationNode> element: edgeList){
            //System.out.print(element.get(0).getStation());
            //System.out.print(" " + depart);
            //System.out.println(element.get(0).getStation().equals(depart));
            if(element.get(0).getStation().equals(depart)){//if you find the station
                return element.get(0);
            }
        }
        return null;
    }
    public int getSize(){
        return numOfNodes;
    }


}