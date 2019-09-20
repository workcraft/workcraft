package org.workcraft.plugins.son.algorithm;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.util.Marking;

import java.util.*;

public class PathAlgorithm {

    private final SON net;
    private static final Collection<Path> pathResult = new ArrayList<>();

    public PathAlgorithm(SON net) {
        this.net = net;
    }

    private void dfs(Collection<Node> nodes, LinkedList<Node> visited, Node v) {
        LinkedList<Node> post = getPostset(visited.getLast(), nodes);

        if (visited.getLast().equals(v)) {
            Path path = new Path();
            path.addAll(visited);
            pathResult.add(path);
        }

        // examine post nodes
        for (Node node : post) {
            if (visited.contains(node)) {
                continue;
            }
            if (node.equals(v)) {
                visited.add(node);
                Path path = new Path();
                path.addAll(visited);
                pathResult.add(path);
                visited.removeLast();
                break;
            }
        }
        // in depth-first, recursion needs to come after visiting post nodes
        for (Node node : post) {
            if (visited.contains(node) || node.equals(v)) {
                continue;
            }
            visited.addLast(node);
            dfs(nodes, visited, v);
            visited.removeLast();

        }
    }

    private void dfsTest(Collection<Node> nodes, LinkedList<Node> visited, Collection<Condition> v) {
        LinkedList<Node> post = getPostset(visited.getLast(), nodes);
        for (Node node : post) {
            visited.add(node);
            if (v.contains(node)) {
                Path path = new Path();
                path.addAll(visited);
                pathResult.add(path);
                visited.removeLast();
            } else if (!visited.contains(node)) {
                dfsTest(nodes, visited, v);
                visited.removeLast();
            }
        }
    }

    //get paths between two given nodes. (recursion)
    public Collection<Path> getPaths(Condition s, Collection<Condition> v, Collection<Node> nodes) {
        pathResult.clear();
        LinkedList<Node> visited = new LinkedList<>();
        visited.add(s);
        dfsTest(nodes, visited, v);
        return pathResult;
    }

    //get paths between two given nodes. (recursion)
    public Collection<Path> getPaths(Node s, Node v, Collection<Node> nodes) {
        pathResult.clear();
        LinkedList<Node> visited = new LinkedList<>();
        visited.add(s);
        dfs(nodes, visited, v);
        return pathResult;
    }

    private LinkedList<Node> getPostset(Node n, Collection<Node> nodes) {
        LinkedList<Node> list = new LinkedList<>();
        for (MathNode post : net.getPostset((MathNode) n)) {
            if (nodes.contains(post)) {
                list.add(post);
            }
        }
        return list;
    }
    //get nodes between two given node sets. (iteration)
    public static Collection<Node> dfs2(Marking s, Marking v, SON net) {
        Collection<Node> result = new HashSet<>();
        RelationAlgorithm relation = new RelationAlgorithm(net);
        Stack<Node> stack = new Stack<>();

        for (Node s1 : s) {
            Collection<Node> visit = new ArrayList<>();
            stack.push(s1);
            visit.add(s1);

            while (!stack.empty()) {
                Node s2 = stack.peek();
                if (v.contains(s2)) {
                    result.add(s2);
                }

                Node post = null;
                for (Node n: relation.getPostPNSet(s2)) {
                    if (result.contains(n)) {
                        result.add(s2);
                    }
                    if (!visit.contains(n)) {
                        post = n;
                        break;
                    }
                }

                if (post != null) {
                    visit.add(post);
                    stack.push(post);
                } else {
                    stack.pop();
                }
            }
        }
        return result;
    }

}

