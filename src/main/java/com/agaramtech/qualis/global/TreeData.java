package com.agaramtech.qualis.global;

import java.util.List;

import com.agaramtech.qualis.testgroup.model.TreeTemplateManipulation;

import lombok.Data;

@Data
public class TreeData {
	private String path;
	private String type;
	private Boolean isRoot;
	private Boolean isOpen;
	private List<String> lstChildren;
	private TreeTemplateManipulation content;
	private String slabelname;
	private int npositioncode;
	
}
