package com.agaramtech.qualis.organization.model;

import java.util.List;
import lombok.Data;

@Data
public class TreeMenuFormat {

	private String key;
	private int primaryKey;
	private int parentKey;
	private String label;
	private String serviceUrl;
	private TreeNodeItem item;
	private List<TreeMenuFormat> nodes;

}
