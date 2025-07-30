package com.agaramtech.qualis.global;

import java.util.List;

import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;

import lombok.Data;

@Data
public class TreeDataFormat {

	private String key;
	private int primaryKey;
	private int parentKey;
	private String label;
	private TreeTemplateManipulation item;
	private List<TreeDataFormat> nodes;	
	
	@Override
	public String toString() {
		return "TreeDataFormat [key=" + key + ", primaryKey=" + primaryKey + ", parentKey=" + parentKey + ", label="
				+ label + ", item=" + item + ", nodes=" + nodes + "]";
	}
	public TreeDataFormat() {
		
	}
	
	public TreeDataFormat(TreeDataFormat objTreeDataFormat) {
		super();
		this.key = objTreeDataFormat.key;
		this.primaryKey = objTreeDataFormat.primaryKey;
		this.parentKey = objTreeDataFormat.parentKey;
		this.label = objTreeDataFormat.label;
		this.item = objTreeDataFormat.item;
		this.nodes = objTreeDataFormat.nodes;
	}
	
	
}
