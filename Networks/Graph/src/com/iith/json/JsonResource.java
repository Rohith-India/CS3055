package com.iith.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.dblp.mmdb.Person;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;
import org.xml.sax.SAXException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/*
 * Generates Graph from set of person names
 */
@Path("/json")
public class JsonResource {
    boolean debug = false;
 
    static Map<String, String> personNameIdMap = new TreeMap<String, String>();

    static RecordDbInterface dblp;
    static String dblpXmlFilename = "dblp.xml.gz";
    static String dblpDtdFilename = "dblp.dtd";

    static {

        loadPersonDetailsFromCsvFle();

        System.out.println("personNameIdMap.size(): " + personNameIdMap.size());

        if(personNameIdMap.size() == 0) {
            loadPersonDetailsFromXmlFle();
        }
    }

    static void loadPersonDetailsFromCsvFle() {
        try {
            long t1 = System.currentTimeMillis();
            BufferedReader br = new BufferedReader(new FileReader("Persons.csv"));
            String line =  null;
            while((line=br.readLine())!=null) {
                if(line.trim().length() > 0) {
                    String arr[] = line.split(",");
                    personNameIdMap.put(arr[0], arr[1]);
                }
            }
            System.out.println("Loaded Persons data in " + (System.currentTimeMillis()- t1)/1000 + " seconds...");
        } catch (final IOException ex) {
            System.err.println("cannot read Persons.csv: " + ex.getMessage());
        }
    }
 
    static void loadPersonDetailsFromXmlFle() {
 
        System.setProperty("entityExpansionLimit", "10000000");
        System.out.println("building the dblp main memory DB ...");

        FileWriter personWriter = null;
        try {
            personWriter = new FileWriter("Persons.csv");
            long t1 = System.currentTimeMillis();
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
            Collection<Person> persons = dblp.getPersons();
            Iterator<Person> iter = persons.iterator();
            while(iter.hasNext()) {
                Person person = iter.next();
                personNameIdMap.put(person.getPrimaryName().name(), person.getPid());
                personWriter.write(person.getPrimaryName() + "," + person.getPid()+"\n");
            }
            personWriter.close();
            System.out.println("Loaded in " + (System.currentTimeMillis()- t1)/1000 + " seconds...");
        }
        catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
        }
        catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
        }
        System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
    }
    
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
        //System.out.println("Graph:\n-----");
        for(int i = 0; i < pids.length-1;i++) {
            List<Publication> pubs_i = pidPublicationsMap.get(pids[i]);
            for(Publication pub_i : pubs_i) {
                for(int j = i+1; j< pids.length; j++) {
                    String key = checkInterSection(pub_i, pids[j], pidPublicationsMap);
                    if(key != null) {
                        String edge = "" + (i+1) + "," + (j+1);// + " " + key;
                        if(!graph.contains(edge)) {
                            //System.out.println(edge);
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
            writeGraphToFile(graph);
            System.out.println("Graph is generated. Check Output.csv file.");
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
    public void writeGraphToFile(List<String> edgeList) {
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
    public String[] readInputFromFile() {
        List<String> inList = new ArrayList<String>();
        try {
            File myObj = new File("Input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String name = myReader.nextLine();
                if(name.trim().length() > 0) {
                    String id = personNameIdMap.get(name);
                    if(id.trim().length() > 0) {
                        inList.add(id);
                    } else {
                        System.out.println("Could not locate the pid for name: " + name);
                    }
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
        String [] pids = instance.readInputFromFile();
        instance.findCoAuthorCombinations(pids);
    }
}