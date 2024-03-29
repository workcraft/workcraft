package org.workcraft.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;

class DirectedGraphUtilsTests {

    private static final Map<Integer, Set<Integer>> graph;

    static {
        graph = new HashMap<>();
        graph.put(1, new HashSet<>(Arrays.asList(2, 5, 8)));
        graph.put(2, new HashSet<>(Arrays.asList(3, 7, 9)));
        graph.put(3, new HashSet<>(Arrays.asList(1, 2, 4, 6)));
        graph.put(4, new HashSet<>(Arrays.asList(5)));
        graph.put(5, new HashSet<>(Arrays.asList(2)));
        graph.put(6, new HashSet<>(Arrays.asList(4)));
        graph.put(7, new HashSet<>(Arrays.asList()));
        graph.put(8, new HashSet<>(Arrays.asList(8, 9)));
        graph.put(9, new HashSet<>(Arrays.asList(8)));
        graph.put(0, new HashSet<>(Arrays.asList(0)));
    }

    private static final Map<File, Set<File>> fileGraph;

    static {
        fileGraph = new HashMap<>();
        fileGraph.put(new File("1"), new HashSet<>(Arrays.asList(new File("2"), new File("5"), new File("8"))));
        fileGraph.put(new File("2"), new HashSet<>(Arrays.asList(new File("3"), new File("7"), new File("9"))));
        fileGraph.put(new File("3"), new HashSet<>(Arrays.asList(new File("1"), new File("2"), new File("4"), new File("6"))));
        fileGraph.put(new File("4"), new HashSet<>(Arrays.asList(new File("5"))));
        fileGraph.put(new File("5"), new HashSet<>(Arrays.asList(new File("2"))));
        fileGraph.put(new File("6"), new HashSet<>(Arrays.asList(new File("4"))));
        fileGraph.put(new File("7"), new HashSet<>(Arrays.asList()));
        fileGraph.put(new File("8"), new HashSet<>(Arrays.asList(new File("8"), new File("9"))));
        fileGraph.put(new File("9"), new HashSet<>(Arrays.asList(new File("8"))));
        fileGraph.put(new File("0"), new HashSet<>(Arrays.asList(new File("0"))));
    }

    @Test
    void reverseDirectedGraphTest() {
        Map<Integer, Set<Integer>> reversedGraph = new HashMap<>();
        reversedGraph.put(1, new HashSet<>(Arrays.asList(3)));
        reversedGraph.put(2, new HashSet<>(Arrays.asList(1, 3, 5)));
        reversedGraph.put(3, new HashSet<>(Arrays.asList(2)));
        reversedGraph.put(4, new HashSet<>(Arrays.asList(3, 6)));
        reversedGraph.put(5, new HashSet<>(Arrays.asList(1, 4)));
        reversedGraph.put(6, new HashSet<>(Arrays.asList(3)));
        reversedGraph.put(7, new HashSet<>(Arrays.asList(2)));
        reversedGraph.put(8, new HashSet<>(Arrays.asList(1, 8, 9)));
        reversedGraph.put(9, new HashSet<>(Arrays.asList(2, 8)));
        reversedGraph.put(0, new HashSet<>(Arrays.asList(0)));
        Assertions.assertEquals(reversedGraph, DirectedGraphUtils.reverse(graph));
    }

    @Test
    void projectOddVerticesTest() {
        Map<Integer, Set<Integer>> oddSubgraph = new HashMap<>();
        oddSubgraph.put(1, new HashSet<>(Arrays.asList(5)));
        oddSubgraph.put(3, new HashSet<>(Arrays.asList(1)));
        oddSubgraph.put(5, new HashSet<>(Arrays.asList()));
        oddSubgraph.put(7, new HashSet<>(Arrays.asList()));
        oddSubgraph.put(9, new HashSet<>(Arrays.asList()));
        Assertions.assertEquals(oddSubgraph, DirectedGraphUtils.project(graph, new HashSet<>(Arrays.asList(1, 3, 5, 7, 9))));
    }

    @Test
    void projectEvenVerticesTest() {
        Map<Integer, Set<Integer>> evenSubgraph = new HashMap<>();
        evenSubgraph.put(2, new HashSet<>(Arrays.asList()));
        evenSubgraph.put(4, new HashSet<>(Arrays.asList()));
        evenSubgraph.put(6, new HashSet<>(Arrays.asList(4)));
        evenSubgraph.put(8, new HashSet<>(Arrays.asList(8)));
        evenSubgraph.put(0, new HashSet<>(Arrays.asList(0)));
        Assertions.assertEquals(evenSubgraph, DirectedGraphUtils.project(graph, new HashSet<>(Arrays.asList(2, 4, 6, 8, 0))));
    }

    @Test
    void findStronglyConnectedComponentsTest() {
        Set<Set<Integer>> components = new HashSet<>();
        components.add(new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6)));
        components.add(new HashSet<>(Arrays.asList(7)));
        components.add(new HashSet<>(Arrays.asList(8, 9)));
        components.add(new HashSet<>(Arrays.asList(0)));
        Assertions.assertEquals(components, DirectedGraphUtils.findStronglyConnectedComponents(graph));
    }

    @Test
    void findSelfloopVerticesTest() {
        Set<Integer> selfloopVertices = new HashSet<>(Arrays.asList(0, 8));
        Assertions.assertEquals(selfloopVertices, DirectedGraphUtils.findSelfloopVertices(graph));
    }

    @Test
    void findFeedbackVerticesTest() {
        Set<Integer> feedbackVertices = new HashSet<>(Arrays.asList(0, 3, 8));
        Assertions.assertEquals(feedbackVertices, DirectedGraphUtils.findFeedbackVertices(graph));
    }

    @Test
    void findLoopedVerticesTest() {
        Set<Integer> loopedVertices = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 8, 9));
        Assertions.assertEquals(loopedVertices, DirectedGraphUtils.findLoopedVertices(graph));
    }

    @Test
    void findSimpleCyclesTest() {
        Set<List<Integer>> cycles = new HashSet<>();
        cycles.add(Arrays.asList(1, 2, 3));
        cycles.add(Arrays.asList(1, 5, 2, 3));
        cycles.add(Arrays.asList(2, 3));
        cycles.add(Arrays.asList(2, 3, 4, 5));
        cycles.add(Arrays.asList(2, 3, 6, 4, 5));
        cycles.add(Arrays.asList(8));
        cycles.add(Arrays.asList(8, 9));
        cycles.add(Arrays.asList(0));
        Assertions.assertEquals(cycles, DirectedGraphUtils.findSimpleCycles(graph));
    }

    @Test
    void findFileSimpleCyclesTest() {
        Set<List<File>> fileCycles = new HashSet<>(); // Starting point in the cycles differ from integer graph
        fileCycles.add(Arrays.asList(new File("3"), new File("1"), new File("2")));
        fileCycles.add(Arrays.asList(new File("3"), new File("1"), new File("5"), new File("2")));
        fileCycles.add(Arrays.asList(new File("3"), new File("2")));
        fileCycles.add(Arrays.asList(new File("3"), new File("4"), new File("5"), new File("2")));
        fileCycles.add(Arrays.asList(new File("3"), new File("6"), new File("4"), new File("5"), new File("2")));
        fileCycles.add(Arrays.asList(new File("8")));
        fileCycles.add(Arrays.asList(new File("9"), new File("8")));
        fileCycles.add(Arrays.asList(new File("0")));
        Assertions.assertEquals(fileCycles, DirectedGraphUtils.findSimpleCycles(fileGraph));
    }

    @Test
    void findRootVerticesTest() {
        Set<Integer> rootVertices = new HashSet<>();
        Assertions.assertEquals(rootVertices, DirectedGraphUtils.findRootVertices(graph));

        Set<File> fileRootVertices = new HashSet<>();
        Assertions.assertEquals(fileRootVertices, DirectedGraphUtils.findRootVertices(fileGraph));

        Map<Integer, Set<Integer>> rootedGraph = new HashMap<>();
        rootedGraph.put(1, new HashSet<>(Arrays.asList(2, 3)));
        rootedGraph.put(2, new HashSet<>(Arrays.asList(3)));
        rootedGraph.put(3, new HashSet<>());

        Set<Integer> oneRootVertices = new HashSet<>(Arrays.asList(1));
        Assertions.assertEquals(oneRootVertices, DirectedGraphUtils.findRootVertices(rootedGraph));

        rootedGraph.put(4, new HashSet<>(3));

        Set<Integer> twoRootVertices = new HashSet<>(Arrays.asList(1, 4));
        Assertions.assertEquals(twoRootVertices, DirectedGraphUtils.findRootVertices(rootedGraph));
    }

    @Test
    void emptyDirectedGraphTest() {
        Map<Integer, Set<Integer>> emptyGraph = new HashMap<>();
        Assertions.assertEquals(new HashMap<>(), DirectedGraphUtils.reverse(emptyGraph));
        Assertions.assertEquals(new HashMap<>(), DirectedGraphUtils.project(emptyGraph, new HashSet<>()));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findStronglyConnectedComponents(emptyGraph));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findSelfloopVertices(emptyGraph));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findFeedbackVertices(emptyGraph));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findLoopedVertices(emptyGraph));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findSimpleCycles(emptyGraph));
        Assertions.assertEquals(new HashSet<>(), DirectedGraphUtils.findRootVertices(emptyGraph));
    }

    @Test
    void nullDirectedGraphTest() {
        Assertions.assertEquals(null, DirectedGraphUtils.reverse(null));
        Assertions.assertEquals(null, DirectedGraphUtils.project(null, null));
        Assertions.assertEquals(null, DirectedGraphUtils.findStronglyConnectedComponents(null));
        Assertions.assertEquals(null, DirectedGraphUtils.findSelfloopVertices(null));
        Assertions.assertEquals(null, DirectedGraphUtils.findFeedbackVertices(null));
        Assertions.assertEquals(null, DirectedGraphUtils.findLoopedVertices(null));
        Assertions.assertEquals(null, DirectedGraphUtils.findSimpleCycles(null));
        Assertions.assertEquals(null, DirectedGraphUtils.findRootVertices(null));
    }

}
