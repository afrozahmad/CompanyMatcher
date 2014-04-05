package com.intuit.ems.hre;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class Matcher {
	
	private Indexer indexer;
	final static  float THRESHOLD = 0.95f;
	public Matcher(String path) throws IOException{
		indexer = new Indexer();
		indexer.init(Paths.get(path));
	}
	
	public Matcher() throws IOException{
		indexer = new Indexer();
	}
	
	public static void main(String[] args) throws IOException {
		if (args == null || args.length!=1){
			System.out.println("/usage: Matcher <pathTocompanyFile>");
			System.exit(1);
		}
		Matcher matcher = new Matcher(args[0]);
		
		
		
//		while (true){
//			System.out.print("Company to check>");
//			Scanner scanIn = new Scanner(System.in);
//			
//			String input = scanIn.nextLine();
//			System.out.print("Address>");
//			
//			scanIn = new Scanner(System.in);
//			String address = scanIn.nextLine();
//
//			System.out.print("state>");
//			scanIn = new Scanner(System.in);
//			String state = scanIn.nextLine();
//
//			System.out.print("city>");
//			scanIn = new Scanner(System.in);
//			String city = scanIn.nextLine();

			
			//Company c = matcher.match(input, city, state, address);
			
			
			Path path = Paths.get("/Users/afroza786/Documents/converted_companies.txt");
		    try (Scanner scanner =  new Scanner(path)){
		    	scanner.nextLine();
		      while (scanner.hasNextLine()){
		        //process each line in some way
		        String row = scanner.nextLine().toLowerCase();
		        
		        String []  data = row.replaceAll("[\\.,']", "" ).split("\t");
		        if (data.length>9){
		        	Company c = matcher.match(data[2], data[3], data[4], data[0]);
			        if (c.score > THRESHOLD){
			        	if (c.fein.replaceAll("^0*", "").equals(data[9].replaceAll("^0*", "").trim())){
			        		System.out.println("TP." + c.name + "\t" + data[2] + "\t" +c.score + "\t" + c.fein + "\t" + data[9]);
			        	}
			        	else {
			        		System.out.println("FP." + c.name + "\t" + data[2] + "\t" +c.score + "\t" + c.fein + "\t" + data[9]);
			        	}
			        }
			        else { // the score was below threshold
			        	if (c.fein.equals(data[9].trim())){ //but it was actually a match
			        		System.out.println("FN." + c.name + "\t" + data[2] + "\t" +c.score + "\t" + c.fein + "\t" + data[9]);
			        	}
			        	else { //and the feins did not match but the right fein is there some where 
			        		
			        		Company com = matcher.searchByFein(data[9]);
			        		if (com != null){
			        			System.out.println("FNE." + com.name + "\t" + data[2] + "\t" +c.score + "\t" + com.fein + "\t" + data[9] + "\t" + com.state + "\t" + data[3]);
			        		}
			        		else {
			        		
			        			System.out.println("TN." + c.name + "\t" + data[2] + "\t" +c.score + "\t" + c.fein + "\t" + data[9]);
			        		}
			        	}

			        }
		        }
		        
		      }      
		    }
			
			
//		}
	}
	public Company match(String name, String city, String state, String address) throws CorruptIndexException, IOException {
		TreeSet<Company> candidate = indexer.suggest(name, 30); 
		
		//from the candidates, narrow down using edit distane
		float maxScore = 0f;
		Company candidateCompany = null;
		Iterator <Company> iter = candidate.iterator();
		while (iter.hasNext()){
			Company c = iter.next();
			float scName = TextUtils.scoreJaroWinkler(c.name, name);
			//float scAddress = TextUtils.scoreLevensthein(c.address1, address);
			float scCity = TextUtils.scoreLevensthein(c.city, city);
			float scState = TextUtils.exactCompare(c.state, state);
			
			float sc  =  (0.5f*scName) + 0.3f*scState + 0.2f*scCity ;//+ 0.1f*scAddress; 
			if (sc>maxScore){
				candidateCompany = c;
				maxScore = sc;
				candidateCompany.score = maxScore;
				
			}
		}
		//return candidateCompany;
		if (maxScore > THRESHOLD){
			candidateCompany.score = maxScore;
			return candidateCompany;
		}
		return null;
		
	}
	
	private Company searchByFein(String fein) throws IOException{
		TreeSet<Company> ts = indexer.searchByFein(fein, 1);
		if (ts !=null && ts.size()>0){
			return ts.first();
		}
		return null;
	}
}
