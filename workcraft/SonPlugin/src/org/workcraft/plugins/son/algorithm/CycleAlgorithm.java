package org.workcraft.plugins.son.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CycleAlgorithm {
    /** number of vertices **/
    private int mV;
    /** preorder number counter **/
    private int preCount;
    /** low number of v **/
    private int[] low;
    /** to check if v is visited **/
    private boolean[] visited;
    /** to store given graph **/
    private List<Integer>[] graph;
    /** to store all scc **/
    private List<List<Integer>> sccComp;
    private Deque<Integer> stack;

    /** function to get all strongly connected components - Tarjan algorithm **/
    public List<List<Integer>> getCycles(List<Integer>[] graph) {
        mV = graph.length;
        this.graph = graph;
        low = new int[mV];
        visited = new boolean[mV];
        stack = new ArrayDeque<>();
        sccComp = new ArrayList<>();

        for (int v = 0; v < mV; v++) {
            if (!visited[v]) {
                dfs(v);
            }
        }
        return sccComp;

    }

    /** function dfs **/
    private void dfs(int v) {
        low[v] = preCount++;
        visited[v] = true;
        stack.push(v);
        int min = low[v];

        for (int w : graph[v]) {
            if (!visited[w]) {
                dfs(w);
            }

            if (low[w] < min) {
                min = low[w];
            }
        }
        if (min < low[v]) {
            low[v] = min;
            return;
        }

        List<Integer> component = new ArrayList<>();
        int w;
        do {
            w = stack.pop();
            component.add(w);
            low[w] = mV;
        } while (w != v);
        sccComp.add(component);
    }
}
