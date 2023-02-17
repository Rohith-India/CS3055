package com.iith.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/*
 * curl -H "Content-Type: application/json" -X POST -d "[\"83/3735\", \"223/7862\", \"g/JimGray\", \"t/AniThakar\", \"25/5416\"]" http://localhost:8080/json/find
 */

@Path("/json")
public class JsonResource {
    boolean debug = false;

    @Path("/find")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> findCoAuthorCombinations(String[] pids) {

        // to store publications for every pid
        Map<String, List<Publication>> pidPublicationsMap = new HashMap<String, List<Publication>>();

        long t1 = System.currentTimeMillis();

        for(int i =0; i < pids.length;i++) {
            // list to store publications for the current pid
            List<Publication> publications = new ArrayList<Publication>();

            // build URL for current person Id
            String urlString = "https://dblp.org/pid/" + pids[i] +".xml";

            try {
                // get XML for the person
                URL url = new URL(urlString);
                URLConnection urlConnection = url.openConnection();
                JAXBContext jaxbContext = JAXBContext.newInstance(Dblpperson.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

                Dblpperson person = (Dblpperson) jaxbUnmarshaller.unmarshal(urlConnection.getInputStream());                
                if(debug) System.out.println("Person: " + pids[i] + " publication size: " + person.getR().length);
                for(R r : person.getR()) {
                    publications.add(r.getPublication());
                }
            }
            catch(Exception e) {
                e.printStackTrace();

            }
            pidPublicationsMap.put(pids[i], publications);
        }
        if(debug) System.out.println("Time taken to extract the details (in secs): " + (System.currentTimeMillis()-t1)/1000);
        t1 = System.currentTimeMillis();

        // to return response in a List of strings
        List<String> graph = new ArrayList<String>();

        // build graph
        System.out.println("Graph:\n-----");
        for(int i = 0; i < pids.length-1;i++) {
            List<Publication> pubs_i = pidPublicationsMap.get(pids[i]);
            for(Publication pub_i : pubs_i) {
                for(int j = i+1; j< pids.length; j++) {
                    String key = checkInterSection(pub_i, pids[j], pidPublicationsMap);
                    if(key != null) {
                        String edge = "" + (i+1) + "," + (j+1);// + " " + key;
                        if(!graph.contains(edge)) {
                            System.out.println(edge);
                            graph.add(edge);
                        }
                    }            
                }
            }
        }
        if(graph.size() == 0) {
            System.out.println("No common edges found with the given input.");
        }
        else {
            writeGraph(graph);
            System.out.println("Graph is created.");
        }
        if(debug) System.out.println("Time taken to build graph (in secs): " + (System.currentTimeMillis()-t1)/1000);

        return graph;
    }

    // to check if there is intersection
    private String checkInterSection(Publication pub_i, String pid_j, Map<String, List<Publication>> pidPublicationsMap) {
        List<Publication> pubs_j = pidPublicationsMap.get(pid_j);
        for(Publication pub_j : pubs_j) {
            if (pub_i.getKey().equals(pub_j.getKey())) {
                return pub_i.getKey();
            }
        }

        return null;
    }

    // Write edges to graph
    public void writeGraph(List<String> edgeList) {
        try {
            FileWriter myWriter = new FileWriter("Output.csv");
            for(String edge : edgeList) {
                myWriter.write(edge);
                myWriter.write("\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // read input
    public String[] readInput() {
        List<String> inList = new ArrayList<String>();
        try {
            File myObj = new File("Input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if(data.trim().length() > 0) {
                    inList.add(data);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return inList.toArray(new String[inList.size()]);
    }

    public static void main(String [] args) {
        JsonResource instance = new JsonResource();
        String [] pids = instance.readInput();
        instance.findCoAuthorCombinations(pids);
    }
}