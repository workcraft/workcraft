package org.workcraft.plugins.dfs.cycle;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class SCCResult {
    private final Vector[] adjList;
    private final int lowestNodeId;

    public SCCResult(Vector[] adjList, int lowestNodeId) {
        this.adjList = adjList;
        this.lowestNodeId = lowestNodeId;
        Set nodeIDsOfSCC = new HashSet();
        if (this.adjList != null) {
            for (int i = this.lowestNodeId; i < this.adjList.length; i++) {
                if (this.adjList[i].size() > 0) {
                    nodeIDsOfSCC.add(Integer.valueOf(i));
                }
            }
        }
    }

    public Vector[] getAdjList() {
        return adjList;
    }

    public int getLowestNodeId() {
        return lowestNodeId;
    }
}
