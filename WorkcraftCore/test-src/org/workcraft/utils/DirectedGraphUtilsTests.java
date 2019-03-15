package org.workcraft.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class DirectedGraphUtilsTests {

    private static final Map<Object, Set<Object>> graph;

    static {
        graph = new HashMap<>();
        graph.put(1, new HashSet<>(Arrays.asList(2, 5, 8)));
        graph.put(2, new HashSet<>(Arrays.asList(3, 7, 9)));
        graph.put(3, new HashSet<>(Arrays.asList(1, 2, 4, 6)));
        graph.put(4, new HashSet<>(Arrays.asList(5)));
        graph.put(5, new HashSet<>(Arrays.asList(2)));
        graph.put(6, new HashSet<>(Arrays.asList(4)));
        graph.put(7, new HashSet<>(Arrays.asList()));
        graph.put(8, new HashSet<>(Arrays.asList(9)));
        graph.put(9, new HashSet<>(Arrays.asList(8)));
        graph.put(0, new HashSet<>(Arrays.asList(0)));
    }

    @Test
    public void reverseDirectedGraphTest() {
        Map<Integer, Set<Integer>> reversedGraph = new HashMap<>();
        reversedGraph.put(1, new HashSet<>(Arrays.asList(3)));
        reversedGraph.put(2, new HashSet<>(Arrays.asList(1, 3, 5)));
        reversedGraph.put(3, new HashSet<>(Arrays.asList(2)));
        reversedGraph.put(4, new HashSet<>(Arrays.asList(3, 6)));
        reversedGraph.put(5, new HashSet<>(Arrays.asList(1, 4)));
        reversedGraph.put(6, new HashSet<>(Arrays.asList(3)));
        reversedGraph.put(7, new HashSet<>(Arrays.asList(2)));
        reversedGraph.put(8, new HashSet<>(Arrays.asList(1, 9)));
        reversedGraph.put(9, new HashSet<>(Arrays.asList(2, 8)));
        reversedGraph.put(0, new HashSet<>(Arrays.asList(0)));
        Assert.assertEquals(reversedGraph, DirectedGraphUtils.reverse(graph));
    }

    @Test
    public void projectOddVerticesTest() {
        Map<Integer, Set<Integer>> oddSubgraph = new HashMap<>();
        oddSubgraph.put(1, new HashSet<>(Arrays.asList(5)));
        oddSubgraph.put(3, new HashSet<>(Arrays.asList(1)));
        oddSubgraph.put(5, new HashSet<>(Arrays.asList()));
        oddSubgraph.put(7, new HashSet<>(Arrays.asList()));
        oddSubgraph.put(9, new HashSet<>(Arrays.asList()));
        Assert.assertEquals(oddSubgraph, DirectedGraphUtils.project(graph, new HashSet<>(Arrays.asList(1, 3, 5, 7, 9))));
    }

    @Test
    public void projectEvenVerticesTest() {
        Map<Integer, Set<Integer>> evenSubgraph = new HashMap<>();
        evenSubgraph.put(2, new HashSet<>(Arrays.asList()));
        evenSubgraph.put(4, new HashSet<>(Arrays.asList()));
        evenSubgraph.put(6, new HashSet<>(Arrays.asList(4)));
        evenSubgraph.put(8, new HashSet<>(Arrays.asList()));
        evenSubgraph.put(0, new HashSet<>(Arrays.asList(0)));
        Assert.assertEquals(evenSubgraph, DirectedGraphUtils.project(graph, new HashSet<>(Arrays.asList(2, 4, 6, 8, 0))));
    }

    @Test
    public void findStronglyConnectedComponentsTest() {
        Set<Set<Integer>> components = new HashSet<>();
        components.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)));
        components.add(new HashSet<>(Arrays.asList(7)));
        components.add(new HashSet<>(Arrays.asList(8, 9)));
        components.add(new HashSet<>(Arrays.asList(0)));
        Assert.assertEquals(components, DirectedGraphUtils.findStronglyConnectedComponents(graph));
    }

    @Test
    public void findSelfloopVerticesTest() {
        Set<Integer> selfloopVertices = new HashSet<>(Arrays.asList(0));
        Assert.assertEquals(selfloopVertices, DirectedGraphUtils.findSelfloopVertices(graph));
    }

    @Test
    public void findFeedbackVerticesTest() {
        Set<Integer> feedbackVertices = new HashSet<>(Arrays.asList(0, 3, 8));
        Assert.assertEquals(feedbackVertices, DirectedGraphUtils.findFeedbackVertices(graph));
    }

    @Test
    public void findSimpleCyclesTest() {
        Set<List<Integer>> cycles = new HashSet<>();
        cycles.add(Arrays.asList(1, 2, 3));
        cycles.add(Arrays.asList(1, 5, 2, 3));
        cycles.add(Arrays.asList(2, 3));
        cycles.add(Arrays.asList(2, 3, 4, 5));
        cycles.add(Arrays.asList(2, 3, 6, 4, 5));
        cycles.add(Arrays.asList(8, 9));
        cycles.add(Arrays.asList(0));
        Assert.assertEquals(cycles, DirectedGraphUtils.findSimpleCycles(graph));
    }

    @Test
    public void emptyDirectedGraphTest() {
        Map<Integer, Set<Integer>> emptyGraph = new HashMap<>();
        Assert.assertEquals(new HashMap<>(), DirectedGraphUtils.reverse(emptyGraph));
        Assert.assertEquals(new HashMap<>(), DirectedGraphUtils.project(emptyGraph, new HashSet<>()));
        Assert.assertEquals(new HashSet<>(), DirectedGraphUtils.findStronglyConnectedComponents(emptyGraph));
        Assert.assertEquals(new HashSet<>(), DirectedGraphUtils.findSelfloopVertices(emptyGraph));
        Assert.assertEquals(new HashSet<>(), DirectedGraphUtils.findFeedbackVertices(emptyGraph));
        Assert.assertEquals(new HashSet<>(), DirectedGraphUtils.findSimpleCycles(emptyGraph));
    }

    @Test
    public void nullDirectedGraphTest() {
        Assert.assertEquals(null, DirectedGraphUtils.reverse(null));
        Assert.assertEquals(null, DirectedGraphUtils.project(null, null));
        Assert.assertEquals(null, DirectedGraphUtils.findStronglyConnectedComponents(null));
        Assert.assertEquals(null, DirectedGraphUtils.findSelfloopVertices(null));
        Assert.assertEquals(null, DirectedGraphUtils.findFeedbackVertices(null));
        Assert.assertEquals(null, DirectedGraphUtils.findSimpleCycles(null));
    }

}
