package de.hpi.ormapping.structures;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassNode {

	@JsonProperty("key")
	public int key;

	@JsonProperty("instanceCount")
	public int instanceCount;

	@JsonProperty("parent")
	public int parent = -1;

	@JsonProperty("className")
	public String className;

	@JsonProperty("type")
	public String type;

	@JsonProperty("fields")
	public List<ClassAttribute> attributes;

	public ClassNode() {
		// TODO Auto-generated constructor stub
	}

	public List<ClassAttribute> collectAllInheritedAttributes(Hierarchy hierarchy) {
		List<ClassAttribute> collectedAttributes = new ArrayList<>();

		collectedAttributes.addAll(attributes);
	
		int next_parent = parent;
		
		while (next_parent > 0) {
			for (ClassNode node : hierarchy.classList) {
				if (node.key == next_parent) {
					collectedAttributes.addAll(node.attributes);
					next_parent = node.parent;
				}
			}
		}
		
		return collectedAttributes;
	}
	
	public List<ClassNode> getAllParentClasses(Hierarchy hierarchy) {
		List<ClassNode> parents = new ArrayList<>();
		
		int next_parent = parent;
		
		while (next_parent > 0) {
			for (ClassNode node : hierarchy.classList) {
				if (node.key == next_parent) {
					parents.add(node);
					next_parent = node.parent;
				}
			}
		}
		
		return parents;
	}
	
	public List<ClassNode> getAllChildClasses(Hierarchy hierarchy) {
		
		List<ClassNode> children = new ArrayList<>();
		
		List<Integer> parentIds = new ArrayList<>();
		List<Integer> nextGenParentIds = new ArrayList<>();
		parentIds.add(key);
		
		while (!parentIds.isEmpty()) {
			
			for (ClassNode node : hierarchy.classList) {
				if (parentIds.contains(node.parent)) {
					children.add(node);
					nextGenParentIds.add(node.key);
				}
			}
			
			parentIds.clear();
			parentIds.addAll(nextGenParentIds);
			nextGenParentIds.clear();
		}
		
		return children;
	}
	
	public List<ClassNode> getAllConcreteChildClasses(Hierarchy hierarchy) {
		
		List<ClassNode> children = new ArrayList<>();
		
		List<Integer> parentIds = new ArrayList<>();
		List<Integer> nextGenParentIds = new ArrayList<>();
		parentIds.add(key);
		
		while (!parentIds.isEmpty()) {
			
			for (ClassNode node : hierarchy.classList) {
				if (parentIds.contains(node.parent)) {
					if(node.type.equals("{concrete}")) {
						children.add(node);
					}
					nextGenParentIds.add(node.key);
				}
			}
			
			parentIds.clear();
			parentIds.addAll(nextGenParentIds);
			nextGenParentIds.clear();
		}
		
		return children;
	}
	
	@Override
	public String toString() {
		return "\n*****\nName: " + className + "\n\tkey: " + key + "\n\tparent: " + parent + "\n\ttype: " + type + "\n\tinstanceCount: " + instanceCount + "\n\tattributes: " + attributes;
	}
}
