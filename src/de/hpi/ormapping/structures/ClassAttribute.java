package de.hpi.ormapping.structures;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassAttribute {

	@JsonProperty("attName")
	public String attName;
	
	@JsonProperty("attType")
	public String attType;
	
	@JsonProperty("attDistCount")
	public String attDistCount;
	
	public ClassAttribute() {
		// TODO Auto-generated constructor stub
	}

	public int getAttDistinctCount() {
		return Integer.parseInt(attDistCount);
	}
}
