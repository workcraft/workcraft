package org.workcraft.plugins.dfs.cycle;

import java.util.List;
import java.util.Vector;

/**
 * Searchs all elementary cycles in a given directed graph. The implementation
 * is independent from the concrete objects that represent the graphnodes, it
 * just needs an array of the objects representing the nodes the graph
 * and an adjacency-matrix of type boolean, representing the edges of the
 * graph. It then calculates based on the adjacency-matrix the elementary
 * cycles and returns a list, which contains lists itself with the objects of the
 * concrete graphnodes-implementation. Each of these lists represents an
 * elementary cycle.<br><br>
 *
 * The implementation uses the algorithm of Donald B. Johnson for the search of
 * the elementary cycles. For a description of the algorithm see:<br>
 * Donald B. Johnson: Finding All the Elementary Circuits of a Directed Graph.
 * SIAM Journal on Computing. Volumne 4, Nr. 1 (1975), pp. 77-84.<br><br>
 *
 * The algorithm of Johnson is based on the search for strong connected
 * components in a graph. For a description of this part see:<br>
 * Robert Tarjan: Depth-first search and linear graph algorithms. In: SIAM
 * Journal on Computing. Volume 1, Nr. 2 (1972), pp. 146-160.<br>
 *
 * @author Frank Meyer, web_at_normalisiert_dot_de
 * @version 1.2, 22.03.2009
 *
 */
public class ElementaryCyclesSearch {
    /** List of cycles */
    private List cycles = null;

    /** Adjacency-list of graph */
    private final int[][] adjList;

    /** Graphnodes */
    private final Object[] graphNodes;

    /** Blocked nodes, used by the algorithm of Johnson */
    private boolean[] blocked = null;

    /** B-Lists, used by the algorithm of Johnson */
    private Vector[] mB = null;

    /** Stack for nodes, used by the algorithm of Johnson */
    private Vector stack = null;

    /**
     * Constructor.
     *
     * @param matrix adjacency-matrix of the graph
     * @param graphNodes array of the graphnodes of the graph; this is used to
     * build sets of the elementary cycles containing the objects of the original
     * graph-representation
     */
    public ElementaryCyclesSearch(boolean[][] matrix, Object[] graphNodes) {
        this.graphNodes = graphNodes;
        this.adjList = AdjacencyList.getAdjacencyList(matrix);
    }

    /**
     * Returns List::List::Object with the Lists of nodes of all elementary
     * cycles in the graph.
     *
     * @return List::List::Object with the Lists of the elementary cycles.
     */
    public List getElementaryCycles() {
        this.cycles = new Vector();
        this.blocked = new boolean[this.adjList.length];
        this.mB = new Vector[this.adjList.length];
        this.stack = new Vector();
        StrongConnectedComponents sccs = new StrongConnectedComponents(this.adjList);
        int s = 0;

        while (true) {
            SCCResult sccResult = sccs.getAdjacencyList(s);
            if (sccResult != null && sccResult.getAdjList() != null) {
                Vector[] scc = sccResult.getAdjList();
                s = sccResult.getLowestNodeId();
                for (int j = 0; j < scc.length; j++) {
                    if ((scc[j] != null) && (scc[j].size() > 0)) {
                        this.blocked[j] = false;
                        this.mB[j] = new Vector();
                    }
                }

                this.findCycles(s, s, scc);
                s++;
            } else {
                break;
            }
        }

        return this.cycles;
    }

    /**
     * Calculates the cycles containing a given node in a strongly connected
     * component. The method calls itself recursivly.
     *
     * @param v
     * @param s
     * @param adjList adjacency-list with the subgraph of the strongly
     * connected component s is part of.
     * @return true, if cycle found; false otherwise
     */
    private boolean findCycles(int v, int s, Vector[] adjList) {
        boolean f = false;
        this.stack.add(Integer.valueOf(v));
        this.blocked[v] = true;

        for (int i = 0; i < adjList[v].size(); i++) {
            int w = ((Integer) adjList[v].get(i)).intValue();
            // found cycle
            if (w == s) {
                Vector cycle = new Vector();
                for (int j = 0; j < this.stack.size(); j++) {
                    int index = ((Integer) this.stack.get(j)).intValue();
                    cycle.add(this.graphNodes[index]);
                }
                this.cycles.add(cycle);
                f = true;
            } else if (!this.blocked[w]) {
                if (this.findCycles(w, s, adjList)) {
                    f = true;
                }
            }
        }

        if (f) {
            this.unblock(v);
        } else {
            for (int i = 0; i < adjList[v].size(); i++) {
                int w = ((Integer) adjList[v].get(i)).intValue();
                if (!this.mB[w].contains(Integer.valueOf(v))) {
                    this.mB[w].add(Integer.valueOf(v));
                }
            }
        }

        this.stack.remove(Integer.valueOf(v));
        return f;
    }

    /**
     * Unblocks recursivly all blocked nodes, starting with a given node.
     *
     * @param node node to unblock
     */
    private void unblock(int node) {
        this.blocked[node] = false;
        Vector bnode = this.mB[node];
        while (bnode.size() > 0) {
            Integer w = (Integer) bnode.get(0);
            bnode.remove(0);
            if (this.blocked[w.intValue()]) {
                this.unblock(w.intValue());
            }
        }
    }
}

