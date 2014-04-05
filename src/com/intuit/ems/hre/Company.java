package com.intuit.ems.hre;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Company implements Comparable<Company>{

	public String city;
	public String name;
	public String state;
	public String address1;
	public float score;
	public String fein;

	public Company(String c, String n, String s, String a, String fein){
		city 		= c;
		name 		= n;
		state 		= s;
		address1 	= a;
		this.fein = fein != null?fein.trim():"";
		
		
	}
	
	@Override
	public int compareTo(Company o) {
		return name.compareTo(o.name);
		
	}
	@Override
	public int hashCode(){
	    return new HashCodeBuilder()
	        .append(name)
	        .append(state)
	        .append(city)
	        .append(address1)
	        .toHashCode();
	}
	
	public void setScore (float f){
		this.score = f;
	}
	
	public String toString(){
		return "name="+name+", state="+state +", city, " + city + ", address=" + address1;
	}
	

	@Override
	public boolean equals(final Object obj){
	    if(obj instanceof Company){
	        final Company other = (Company) obj;
	        return new EqualsBuilder()
	            .append(name, other.name)
	            .append(state, other.state)
	            .append(city, other.city)
	            .append(address1, other.address1)
	            .isEquals();
	    } else{
	        return false;
	    }
	}

}
