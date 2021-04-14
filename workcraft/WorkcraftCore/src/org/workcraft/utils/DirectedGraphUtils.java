package org.workcraft.utils;

import java.util.*;

public class DirectedGraphUtils {

    private static class Block<T> {
        public final Set<T> set = new HashSet<>();
        public final Map<T, Set<T>> map = new HashMap<>();
    }

    public static <T> Map<T, Set<T>> project(Map<T, Set<T>> graph, Set<T> vertices) {
        if ((graph == null) || (vertices == null)) {
            return null;
        }
        Map<T, Set<T>> result = new HashMap<>();
        for (T vertex : graph.keySet()) {
            if (vertices.contains(vertex)) {
                Set<T> postset = new HashSet<>(graph.get(vertex));
                postset.retainAll(vertices);
                result.put(vertex, postset);
            }
        }
        return result;
    }

    public static <T> Map<T, Set<T>> reverse(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Map<T, Set<T>> result = new HashMap<>();
        for (T currentVertex : graph.keySet()) {
            for (T nextVertex : graph.get(currentVertex)) {
                Set<T> postset = result.computeIfAbsent(nextVertex, k -> new HashSet<>());
                postset.add(currentVertex);
            }
        }
        return result;
    }

    /**
     * Implementation of Johnson algorithm for finding all simple cycles in
     * a directed graph specified by its adjacency list.
     *
     * Time complexity: O(V+E)*(C+1), where C is number of cycles.
     * Space complexity: O(V+E+S), where S is the sum of all cycles' lengths.
     **/
    public static <T> Set<List<T>> findSimpleCycles(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Set<List<T>> result = new HashSet<>();
        Set<T> vertices = new HashSet<>(graph.keySet());
        while (!graph.isEmpty()) {
            Set<Set<T>> components = findStronglyConnectedComponents(graph);
            Set<T> maxComponent = null;
            for (Set<T> component : components) {
                if (component.size() < 2) {
                    if (isSelfCycle(graph, component)) {
                        result.add(new ArrayList<>(component));
                    }
                    vertices.removeAll(component);
                } else if ((maxComponent == null) || (component.size() > maxComponent.size())) {
                    maxComponent = component;
                }
            }
            if (maxComponent != null) {
                Map<T, Set<T>> subgraph = project(graph, maxComponent);
                T startVertex = maxComponent.iterator().next();
                result.addAll(findSimpleCycles(subgraph, startVertex));
                vertices.remove(startVertex);
            }
            graph = project(graph, vertices);
        }
        return result;
    }

    public static <T> Set<T> findSelfloopVertices(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Set<T> result = new HashSet<>();
        for (T vertex : graph.keySet()) {
            if (graph.get(vertex).contains(vertex)) {
                result.add(vertex);
            }
        }
        return result;
    }

    private static <T> boolean isSelfCycle(Map<T, Set<T>> graph, Set<T> component) {
        if (component.size() == 1) {
            return isSelfCycle(graph, component.iterator().next());
        }
        return false;
    }

    private static <T> boolean isSelfCycle(Map<T, Set<T>> graph, T vertex) {
        Set<T> postset = graph.get(vertex);
        if (postset != null) {
            return postset.contains(vertex);
        }
        return false;
    }

    private static <T> Set<List<T>> findSimpleCycles(Map<T, Set<T>> graph, T startVertex) {
        Stack<T> stack = new Stack<>();
        Block<T> block = new Block<>();
        return findSimpleCycles(graph, startVertex, startVertex, stack, block);
    }

    private static <T> Set<List<T>> findSimpleCycles(Map<T, Set<T>> graph, T startVertex,
            T currentVertex, Stack<T> stack, Block<T> block) {

        Set<List<T>> result = new HashSet<>();
        stack.push(currentVertex);
        block.set.add(currentVertex);

        for (T nextVertex : graph.get(currentVertex)) {
            if (nextVertex.equals(startVertex)) {
                result.add(new ArrayList<>(stack));
            } else if (!block.set.contains(nextVertex)) {
                result.addAll(findSimpleCycles(graph, startVertex, nextVertex, stack, block));
            }
        }

        if (!result.isEmpty()) {
            unblock(currentVertex, block);
        } else {
            for (T nextVertex : graph.get(currentVertex)) {
                Set<T> blockedSetForVertex = block.map.computeIfAbsent(nextVertex, k -> new HashSet<>());
                blockedSetForVertex.add(currentVertex);
            }
        }

        stack.pop();
        return result;
    }

    private static <T> void unblock(T vertex, Block<T> block) {
        if (block.set.remove(vertex)) {
            Set<T> blockedSetForVertex = block.map.remove(vertex);
            if (blockedSetForVertex != null) {
                blockedSetForVertex.forEach(v -> unblock(v, block));
            }
        }
    }

    /**
     * Implementation of Kosaraju algorithm for finding strongly connected components
     * in a directed graph specified by its adjacency list.
     *
     * Time complexity: O(V+E)
     * Space complexity: O(V)
     */
    public static <T>  Set<Set<T>> findStronglyConnectedComponents(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Set<T> visited = new HashSet<>();
        Stack<T> stack = new Stack<>();
        for (T vertex : graph.keySet()) {
            if (!visited.contains(vertex)) {
                fillStack(graph, vertex, visited, stack);
            }
        }

        Map<T, Set<T>> reversedGraph = reverse(graph);
        visited.clear();
        Set<Set<T>> result = new HashSet<>();
        while (!stack.isEmpty()) {
            T vertex = stack.pop();
            if (!visited.contains(vertex)) {
                Set<T> component = new HashSet<>();
                fillComponent(reversedGraph, vertex, visited, component);
                result.add(component);
            }
        }
        return result;
    }

    private static <T> void fillStack(Map<T, Set<T>> graph, T startVertex, Set<T> visited, Stack<T> stack) {
        visited.add(startVertex);
        for (T vertex : graph.get(startVertex)) {
            if (!visited.contains(vertex)) {
                fillStack(graph, vertex, visited, stack);
            }
        }
        stack.push(startVertex);
    }

    private static <T> void fillComponent(Map<T, Set<T>> graph, T startVertex, Set<T> visited, Set<T> component) {
        visited.add(startVertex);
        component.add(startVertex);
        if (graph.containsKey(startVertex)) {
            for (T vertex : graph.get(startVertex)) {
                if (!visited.contains(vertex)) {
                    fillComponent(graph, vertex, visited, component);
                }
            }
        }
    }

    public static <T> Set<T> findFeedbackVertices(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Set<T> result = findSelfloopVertices(graph);
        Set<T> vertices = new HashSet<>(graph.keySet());
        while (!graph.isEmpty()) {
            vertices.removeAll(result);
            graph = project(graph, vertices);
            for (Set<T> component : findStronglyConnectedComponents(graph)) {
                if (component.size() == 1) {
                    vertices.removeAll(component);
                } else {
                    Map<T, Set<T>> subgraph = project(graph, component);
                    Map<T, Set<T>> reverseSubgraph = reverse(graph);
                    T bestVertex = null;
                    int bestCount = -1;
                    for (T vertex : component) {
                        int count = subgraph.get(vertex).size() + reverseSubgraph.get(vertex).size();
                        if (count > bestCount) {
                            bestVertex = vertex;
                            bestCount = count;
                        }
                    }
                    if (bestVertex != null) {
                        result.add(bestVertex);
                    }
                }
            }
        }
        return result;
    }

    public static <T> Set<T> findLoopedVertices(Map<T, Set<T>> graph) {
        if (graph == null) {
            return null;
        }
        Set<T> result = findSelfloopVertices(graph);
        Set<T> vertices = new HashSet<>(graph.keySet());
        graph = project(graph, vertices);
        for (Set<T> component : findStronglyConnectedComponents(graph)) {
            if (component.size() > 1) {
                result.addAll(component);
            }
        }
        return result;
    }

}
