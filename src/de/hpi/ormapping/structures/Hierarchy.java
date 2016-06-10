package de.hpi.ormapping.structures;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "class" })
public class Hierarchy {

	@JsonProperty("nodeDataArray")
	public List<ClassNode> classList;

	public Hierarchy() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String toString() {
		StringBuilder builder= new StringBuilder();
		for(ClassNode node : classList) {
			builder.append(node.toString());
		}
		return builder.toString();
	}
}
