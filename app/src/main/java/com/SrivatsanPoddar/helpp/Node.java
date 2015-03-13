package com.SrivatsanPoddar.helpp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/*
 * Retrofit object representing a node in the instruction tree
 */
@SuppressWarnings("serial")
public class Node implements Serializable{

	private int node_id;
	private int parent_node_id;
	private String display_text;
	private String phone_number;
	private ArrayList<Node> children;
	private String node_type;

	public Node(int nodeID, int parentNodeID, String displayText, String phoneNumber, String companyId, String nodeType) {
		node_id = nodeID;
		parent_node_id = parentNodeID;
		display_text = displayText;
		phone_number = phoneNumber;
		children = new ArrayList<Node>();
        node_type = nodeType;
	}

    //Initialize children
	public void initChildren()
	{
		children = new ArrayList<Node>();
	}

    //Retrives the children of this node
	public Node[] getChildren() {
        //Don't sort the main list --CHANGE THIS--POOR PROGRAMMING
        if (children.size() < 15) {
            Collections.sort(children, new NodeComparator());
        }

        return children.toArray(new Node[children.size()]);
    }

    //Add a child to this node
	public void addChild(Node child)
	{
		assert children != null;
		children.add(child);
	}

    //Get the ID of this node
	public int getNodeId(){
		return node_id;
	}

    //Get the ID of the parent node
	public int getParentNodeId() {
		return parent_node_id;
	}
	
	@Override
	public String toString() {
		return display_text;
	}

	public String getPhoneNumber() {
		return phone_number;
	}

    public String getNodeType() { return node_type; }
}

