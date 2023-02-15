package com.iith.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dblp.mmdb.Person;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.Publication;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;
import org.xml.sax.SAXException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/json")
public class JsonResource {

	static RecordDbInterface dblp;
	static String dblpXmlFilename = "dblp-2019-04-01-test.xml";
	static String dblpDtdFilename = "dblp-2017-08-29.dtd";
	static {

		System.setProperty("entityExpansionLimit", "10000000");
        System.out.println("building the dblp main memory DB ...");
        try {
        	long t1 = System.currentTimeMillis();
        	System.out.println("Working Directory = " + System.getProperty("user.dir"));
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
            System.out.println("Loaded in " + (System.currentTimeMillis()- t1)/1000 + " seconds...");
        }
        catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
            //return;
        }
        catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
            //return;
        }
        System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
	}

    @Path("/find")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<List<String>, List<String>> findCoAuthorCombinations(String[] authorNames) {
    	Map<List<String>, List<String>> returnMap = new HashMap<List<String>, List<String>>();

    	List<Person> persons = new ArrayList<Person>();
    	for(String authorName : authorNames) {
    		Person per = dblp.getPersonByName(authorName);
    		persons.add(per);
    	}
    	
    	Collection<Publication> pubs = dblp.getPublications();
    	for(Publication pub : pubs) {
    		List<PersonName> personNames = pub.getNames();
    		List<String> key = new ArrayList<String>();
    		for(int i=0; i < authorNames.length; i++) {
    			for(PersonName personName : personNames) {
    				if(personName.getPrimaryName().equals(persons.get(i).getPrimaryName())) {
    					key.add(authorNames[i]);
    					break;
    				}
    			}
    		}
    		if(key.size() > 1) {
    			List<String> pubList = returnMap.get(key);
    			if(pubList == null) {
    				pubList = new ArrayList<String>();
    				returnMap.put(key, pubList);
    			}
    			System.out.println(pub.getXml());
    			pubList.add(pub.toString());
    		}
    	}
    	
    	
    	/*
    	List<Person> persons = new ArrayList<Person>();
    	Map<Person, Collection<Publication>> authorPublications = new HashMap<Person, Collection<Publication>>();
    	// get and store publications for each author
    	for(String authorName : authorNames) {
    		Person person = dblp.getPersonByName(authorName);
    		persons.add(person);
    		Collection<Publication> pubs = dblp.getPublications(person);
    		System.out.println("authorName: " + authorName + " pubs.size(): " + pubs.size());
    		if(pubs.size() > 0) {
    			authorPublications.put(person, pubs);
    		}
     	}

		Map<String, Set<Publication>> finalIntersectionMap = new HashMap<String, Set<Publication>>();
		Set<Publication> intersections = new HashSet<Publication>();
 		Set<Publication> tempIntersections = new HashSet<Publication>();
    	for(int i = 0; i < persons.size()-1; i++) {
    		int k = i;
    		while(k<persons.size()) {
    		String baseKey = persons.get(i).getPrimaryName().name();
    		System.out.println("Author_i: " + (i+1) + " " + baseKey);
    		Collection<Publication> coll_i = authorPublications.get(persons.get(i));
    		intersections.clear();
     		tempIntersections.clear();
     		tempIntersections.addAll(coll_i);
 
    		for(int j = k+1; j < persons.size(); j++) {
    			Collection<Publication> coll_j = authorPublications.get(persons.get(j));
    			System.out.println("Author_j: " + (j+1) + " " + persons.get(j).getPrimaryName().name());
    			// find intersections with j_th author
     			tempIntersections.retainAll(coll_j);
     			System.out.println("tempIntersection.size(): " + tempIntersections.toString());
     			// if there are common publications
     			if(tempIntersections.size() > 0) {
     				System.out.println("Before: intersection.size(): " + intersections.toString());
     			    intersections.removeAll(tempIntersections);
     			    System.out.println("After: intersection.size(): " + intersections.toString());
     			    if(intersections.size() > 0) {
     			    	System.out.println("111:baseKey: " + baseKey + " intersection.size(): " + intersections.size());
     			    	finalIntersectionMap.put(baseKey, new HashSet<Publication>(intersections));
     			    }
     				baseKey += " | " + persons.get(j).getPrimaryName().name();
     				intersections.clear();
     				intersections.addAll(tempIntersections);
     				if(j == persons.size()-1) {
     					System.out.println("222:baseKey: " + baseKey + " intersection.size(): " + intersections.size());
 	 					finalIntersectionMap.put(baseKey, new HashSet<Publication>(intersections));
 	 					baseKey = persons.get(i).getPrimaryName().name();
 	 					intersections.clear();
     				}
     			}
     			// if there are no intersections
     			else {
     	    		System.out.println("111: intersection.size(): " + intersections.size());
     	 			if(intersections.size() > 0) {
     	 				Iterator<Set<Publication>> iter = finalIntersectionMap.values().iterator();
     	 				while(iter.hasNext())
     	 				{
     	 					intersections.removeAll(iter.next());
     	 				}
     	 				System.out.println("222: intersection.size(): " + intersections.size());
     	 				if(intersections.size() > 0) {
     	 					System.out.println("333:baseKey: " + baseKey + " intersection.size(): " + intersections.size());
     	 					finalIntersectionMap.put(baseKey, new HashSet<Publication>(intersections));
     	 					baseKey = persons.get(i).getPrimaryName().name();
     	 					intersections.clear();
     	 				}
     				}
     	 			// reset
     	 			tempIntersections.addAll(coll_i);
     	 			baseKey = persons.get(i).getPrimaryName().name();
     			}
    		}
    		k++;
    		}
    	}
 
    	Set<String> keys = finalIntersectionMap.keySet();
    	System.out.println("###########################################################################################################");
    	for(String key: keys) {
    		List<String> pubList = new ArrayList<String>();
    		System.out.println("key: " + key);
    		Set<Publication> pubs = finalIntersectionMap.get(key);
    		Iterator<Publication> iter = pubs.iterator();
    		while(iter.hasNext()) {
    			Publication pub = iter.next();
    			System.out.println(pub.getXml());
    			//System.out.println("pub.getBooktitle(): " + (pub.getBooktitle()==null ?"":pub.getBooktitle().getTitle()) + " pub.getJournal(): " + (pub.getJournal()==null?"":pub.getJournal().getTitle()) + " pub.getPublicationStream(): " + pub.getPublicationStream().getTitle());
    			pubList.add(pub.toString());
    		}
    		returnMap.put(key, pubList);
    		System.out.println("###########################################################################################################");
    	}
    	*/
        return returnMap;

    }
}