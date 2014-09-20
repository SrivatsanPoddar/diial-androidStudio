package com.SrivatsanPoddar.helpp;

import java.util.Comparator;

/**
 * Created by Piyush on 9/19/2014.
 * Compares nodes for sorting by names
 */
public class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node o1, Node o2) {
        return o1.getNodeId() - o2.getNodeId();
    }
}