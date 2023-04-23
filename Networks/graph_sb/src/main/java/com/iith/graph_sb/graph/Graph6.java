package com.iith.graph_sb.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;

public class Graph6 {
    private int V;
    private List<List<Integer>> adj;

    public Graph6(int V) {
        this.V = V;
        adj = new ArrayList<>(V);
        for (int i = 0; i < V; i++) {
            adj.add(new ArrayList<>());
        }
    }

    public void addEdge(int u, int v) {
        adj.get(u).add(v);
        adj.get(v).add(u);
    }

    public double averageDistance() {
        double totalDistance = 0;
        int diameter = 0;
        for (int i = 0; i < V; i++) {
            long t1 = System.currentTimeMillis();
            int[] dist = new int[V];
            Arrays.fill(dist, -1);
            Queue<Integer> q = new LinkedList<>();
            q.offer(i);
            dist[i] = 0;
            while (!q.isEmpty()) {
                int u = q.poll();
                for (int v : adj.get(u)) {
                    if (dist[v] == -1) {
                        dist[v] = dist[u] + 1;
                        q.offer(v);
                    }
                }
            }
            for (int j = 0; j < V; j++) {
                if (i != j) {
                    totalDistance += dist[j];
                }
                if(dist[j] > diameter) diameter = dist[j];
                //System.out.println("totalDistance: " + dist[j]);
            }
            if((i+1)%1000 == 0) {
                System.out.print(new StringBuilder("i:").append(i+1).append(" ").append(totalDistance).append(" d: ").append(diameter).toString());
                System.out.println(" in seconds: " + (System.currentTimeMillis()-t1)/1000);
                t1 = System.currentTimeMillis();
            }
        }
        return totalDistance / (V * (V - 1));
    }
    public double clusteringCoefficient() {
        double total = 0.0;
        for (int v = 0; v < V; v++) {
            int deg = degree(v);
            if(deg == 0) continue;
            // Cumulate local clustering coefficient of vertex v.
            int possible = deg * (deg - 1);
            int actual = 0;
            for (int u : adj.get(v)) {
                for (int w : adj.get(v)) {
                    
                    if (adj.get(u).contains(w))
                        actual++;
                }
            }
            if (possible > 0) {
                total += 1.0 * actual / possible;
            }
        }
        return total / V;
    }
    
    public int degree(int v) {
        return adj.get(v).size();
    }
    
    public void degreeDistribution() {
        TreeMap<Integer, Integer> degreeDistributionMap = new TreeMap<Integer, Integer>();
        for (int v = 0; v < V; v++) {
            int degree = degree(v);
            //System.out.println("v: " + v + " " + degree);
            int count = 0;
            if(degree != -1) {
                if(degreeDistributionMap.containsKey(degree)) {
                    count = degreeDistributionMap.get(degree);
                }
                degreeDistributionMap.put(degree, ++count); 
            }
        }
        Iterator<Integer> iter = degreeDistributionMap.keySet().iterator();
        while(iter.hasNext()) {
            int degree = iter.next();
            int count = degreeDistributionMap.get(degree);
            System.out.println("degree: " + degree + " count: " + count);
        }
    }

    public static void main(String [] args) {
        /*
        Graph6 g = new Graph6(6);
        g.addEdge(0, 1);
        g.addEdge(0, 2);
        g.addEdge(1, 3);
        g.addEdge(1, 5);
        g.addEdge(2, 4);
        g.addEdge(3, 5);
        g.addEdge(4, 5);*/
        String fileName = "AuthorGraph.csv";
        int vCount = 3218009;
        System.out.println(args.length);
        if(args.length > 1) {
            fileName = args[0];
           vCount = Integer.parseInt(args[1]);
        }
        System.out.println(fileName + " " + vCount);
        Graph6 g = new Graph6(vCount);
        try {
            File csvFile = new File(fileName);
            Scanner scanner = new Scanner(csvFile);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");
                int src = Integer.parseInt(values[0]);
                int dest = Integer.parseInt(values[1]);
                //System.out.println("src: " + src + ", dest: " + dest);
                g.addEdge(src-1, dest-1);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
        long t1 = System.currentTimeMillis();
        g.degreeDistribution();
        System.out.println("in seconds: " + (System.currentTimeMillis()-t1)/1000);
        t1 = System.currentTimeMillis();
        double clusteringCoefficient = g.clusteringCoefficient();
        System.out.println("clusteringCoefficient: " + clusteringCoefficient + " in seconds: " + (System.currentTimeMillis()-t1)/1000);
        double avgDistance = g.averageDistance();
        System.out.println("Average Distance: " + avgDistance);
    }
}
