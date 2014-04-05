package com.intuit.ems.hre;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.NGramDistance;
import org.apache.lucene.search.spell.StringDistance;

public class TextUtils {
	
	
	

	public static float exactCompare(String one, String two) {
		if (one == null && two==null) return 1;
		if (one == null || two==null) return 0;
		
		return one.equalsIgnoreCase(two)?1:0;
		
	}
	
	public static float scoreLevensthein(String a, String b){
		return stringDistance(new LevensteinDistance(), a, b);
	}
	
	public static float scoreJaroWinkler(String a, String b){
		return stringDistance(new JaroWinklerDistance(), a, b);
	}
	
	public static float scoreNgramEditDistance(String a, String b){
		return stringDistance(new NGramDistance(), a, b);
	}
	
	private static float stringDistance(StringDistance algo, String one, String two){
		return algo.getDistance(one.toLowerCase(),  two.toLowerCase());
	}
	
	public static float scoreJaccard(Set<String> one, Set<String> two){
		Set <String> union = new HashSet<>();
		union.addAll(one);
		union.addAll(two);
		
		Set <String> intersection = new HashSet<>();
		intersection.addAll(one);
		intersection.retainAll(two);
		
		return  (float)intersection.size()/(float)union.size();
	
		
	}
	
	
	
	public static double lcs(String one, String two) {
	      int maxLength = one.length() > two.length() ? one.length() : two.length();
	        int[][] lengths = new int[one.length() + 1][two.length() + 1];
	        
	        // row 0 and column 0 are initialized to 0 already
	        
	        for (int i = 0; i < one.length(); i++) {
	            for (int j = 0; j < two.length(); j++) {
	                if (one.charAt(i) == two.charAt(j)) {
	                    lengths[i + 1][j + 1] = lengths[i][j] + 1;
	                } else {
	                    lengths[i + 1][j + 1] = Math.max(lengths[i + 1][j], lengths[i][j + 1]);
	                }
	            }
	        }
	        
	        return ((double)lengths[one.length()][two.length()])/maxLength;
	      
	}
	
	/*
	 * actual score instead of normalized
	 */
	public static float  editDistance(String a, String b){
		LevensteinDistance ld = new LevensteinDistance();
		float d = ld.getDistance(a, b);
		
		return (float) (Math.max(a.length(), b.length()) - d*Math.max(a.length(), b.length()));

	}
	
    public static List<String> findAll(Pattern regexp, String text) {
    	final List<String> result = new ArrayList<>(); 
    	StringBuffer sb = new StringBuffer();
        Matcher matcher = regexp.matcher(text);
        while (matcher.find()) {
        	matcher.appendReplacement(sb, matcher.group());
        }
        return result;
    }
	public static void main(String[] args) throws IOException {
		TextUtils tu = new  TextUtils();
		String one = "Miartha ";
		String two = "Martha tllc" ;
		String three = "Martha" ;
		
		System.out.println(tu.lcs(one,two));
		System.out.println(tu.scoreLevensthein(one,two));
		System.out.println(tu.scoreNgramEditDistance(one,two));
		System.out.println(tu.scoreJaroWinkler(one,two));
		System.out.println(tu.scoreJaroWinkler(three,two));
		
	}
	
	
	
}
