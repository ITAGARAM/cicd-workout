package com.agaramtech.qualis.organization.model;

import lombok.Data;

@Data
public class TreeNodeItem {

	private String selectedNode;
	private String primaryKeyField;
	private int primaryKeyValue;
	private String selectedNodeName;
	private Object selectedNodeDetail;

}
