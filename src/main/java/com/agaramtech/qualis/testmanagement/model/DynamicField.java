package com.agaramtech.qualis.testmanagement.model;


public class DynamicField {
	
	private String sparameter;
	private String svalues;
	private int nisaverageneed;
	private int ntransactionsamplecode;
	private boolean senableAverage;
	private String soldvalue;
	
	
	public String getSoldvalue() {
		return soldvalue;
	}
	public void setSoldvalue(String soldvalue) {
		this.soldvalue = soldvalue;
	}
	
	public boolean getSenableAverage() {
		return senableAverage;
	}
	
	public void setSenableAverage(boolean senableAverage) {
		this.senableAverage = senableAverage;
	}
	
	public String getSparameter() {
		return sparameter;
	}
	public void setSparameter(String sparameter) {
		this.sparameter = sparameter;
	}
	public String getSvalues() {
		return svalues;
	}
	public void setSvalues(String svalues) {
		this.svalues = svalues;
	}
	
	public int getNisaverageneed() {
		return nisaverageneed;
	}
	public void setNisaverageneed(int nisaverageneed) {
		this.nisaverageneed = nisaverageneed;
	}
	public void setNtransactionsamplecode(int ntransactionsamplecode) {
		this.ntransactionsamplecode = ntransactionsamplecode;
	}

}
