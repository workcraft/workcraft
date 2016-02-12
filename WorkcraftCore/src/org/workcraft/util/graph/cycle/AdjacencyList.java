package org.workcraft.util.graph.cycle;

import java.util.Vector;

/**
 * Calculates the adjacency-list for a given adjacency-matrix.
 *
 *
 * @author Frank Meyer, web@normalisiert.de
 * @version 1.0, 26.08.2006
 *
 */
public class AdjacencyList {
    /**
     * Calculates a adjacency-list for a given array of an adjacency-matrix.
     *
     * @param adjacencyMatrix array with the adjacency-matrix that represents
     * the graph
     * @return int[][]-array of the adjacency-list of given nodes. The first
     * dimension in the array represents the same node as in the given
     * adjacency, the second dimension represents the indicies of those nodes,
     * that are direct successornodes of the node.
     */
    public static int[][] getAdjacencyList(boolean[][] adjacencyMatrix) {
        int[][] list = new int[adjacencyMatrix.length][];

        for (int i = 0; i < adjacencyMatrix.length; i++) {
            Vector v = new Vector();
            for (int j = 0; j < adjacencyMatrix[i].length; j++) {
                if (adjacencyMatrix[i][j]) {
                    v.add(new Integer(j));
                }
            }

            list[i] = new int[v.size()];
            for (int j = 0; j < v.size(); j++) {
                Integer in = (Integer) v.get(j);
                list[i][j] = in.intValue();
            }
        }

        return list;
    }
}
