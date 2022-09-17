package com.example.myway;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Arrays;

public class Dijkstra {

    private ListGraph graph;
    public int distance[];
    private HashSet Set;
    private HashMap<Integer, Integer> prevMap;
    private StationNode depart;
    private String arrival;
    public int ntime=0;

    public String stations;

    public Dijkstra(ListGraph g, StationNode d, String a){
        depart=d;
        arrival=a;
        graph = g;
        Set= new HashSet();
        prevMap = new HashMap();
        int start= depart.getVertex();
        Set.add(start);//add the first starting node's vertex number
        distance = new int[graph.getSize()];
        for(int v=0; v< graph.getSize(); v++){
            distance[v]=-1;//initialize all the vertex to infinite
        }
    }

    @SuppressWarnings("unchecked")
    public void disjkstra(){
        LinkedList<StationNode> edgelist= new LinkedList<StationNode>();
        edgelist=(LinkedList<StationNode>)graph.getEdges(depart).clone();
        edgelist.remove(0);//remove the first node in order that only the list of edges remains
        for(StationNode edge: edgelist){
            distance[edge.getVertex()]=edge.getInterval();//change the distance of edge to t
        }


        for(int i=0; i<distance.length; i++){
            int small=SmallestDistance(distance);//station node's vertex number that has the smallest distance
            Set.add(small);
            //u: the edge vertexes of small....
            int c=0;

            LinkedList<StationNode> uedgelist= new LinkedList<StationNode>();
            uedgelist=(LinkedList<StationNode>) graph.getEdges(small).clone();//find the edges of small node (num,bvertex,time interval)

            for(StationNode node: uedgelist){
                //ntime = ntime + node.getInterval();
                if(Set.contains(node.getVertex()))
                    continue;
                if(distance[node.getVertex()]==-1||distance[node.getVertex()]>distance[small]+node.getInterval()){
                    distance[node.getVertex()]=distance[small]+node.getInterval();
                    StationNode prev=graph.findNode(small);
                    prevMap.put(node.getVertex(), prev.getVertex());

                    if(node.getStation().equals(arrival)){
                        System.out.println(depart.getStation());
                        traverseStations(node.getVertex());
                        System.out.println(arrival);
                        ntime = distance[node.getVertex()];


                        return;//end the method
                    }
                }
            }


        }

        //follow the track

    }

    public void traverseStations(int vertex){
        stations += graph.findNode(prevMap.get(vertex)).getStation() + ",";
        System.out.println(graph.findNode(prevMap.get(vertex)).getStation());
        if(prevMap.containsKey(prevMap.get(vertex)))
            traverseStations(prevMap.get(vertex));
    }


    public int SmallestDistance(int input[]){//find the smallest distance[v]
        int minValue=-1;
        int min=-1;
        for(int i=0; i<input.length; i++){
            //System.out.print(i);
            //System.out.print(" ");
            //System.out.println(input[i]);
            if(input[i]==-1 || Set.contains(i))//s.t.v is not in S
                continue;
            if(input[i]<minValue || minValue==-1 ){
                minValue=input[i];
                min=i;
            }
        }
        return min;
    }



}
