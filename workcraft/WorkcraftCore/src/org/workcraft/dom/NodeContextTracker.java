package org.workcraft.dom;

import org.workcraft.observation.*;

import java.util.*;

public class NodeContextTracker<N extends Node, C extends Connection> extends HierarchySupervisor implements NodeContext<N, C> {
    private final HashMap<N, LinkedHashSet<N>> presets = new HashMap<>();
    private final HashMap<N, LinkedHashSet<N>> postsets = new HashMap<>();
    private final HashMap<N, LinkedHashSet<C>> connections = new HashMap<>();

    private void initHashes(N n) {
        presets.computeIfAbsent(n, s -> new LinkedHashSet<>());
        postsets.computeIfAbsent(n, s -> new LinkedHashSet<>());
        connections.computeIfAbsent(n, s -> new LinkedHashSet<>());
    }

    private void removeHashes(Node n) {
        presets.remove(n);
        postsets.remove(n);
        connections.remove(n);
    }

    @SuppressWarnings("unchecked")
    private void nodeAdded(Node n) {
        initHashes((N) n);

        if (n instanceof Connection) {
            C con = (C) n;
            N c1 = (N) con.getFirst();
            N c2 = (N) con.getSecond();

            initHashes(c1);
            initHashes(c2);

            postsets.get(c1).add(c2);
            presets.get(c2).add(c1);
            connections.get(c1).add(con);
            connections.get(c2).add(con);
        }

        for (Node nn : n.getChildren()) {
            nodeAdded(nn);
        }
    }

    private void nodeRemoved(Node node) {
        LinkedHashSet<N> nodePostset = postsets.get(node);
        if (nodePostset != null) {
            for (N succNode: nodePostset) {
                LinkedHashSet<N> succNodePreset = presets.get(succNode);
                if (succNodePreset != null) {
                    succNodePreset.remove(node);
                }
            }
        }
        LinkedHashSet<N> nodePreset = presets.get(node);
        if (nodePreset != null) {
            for (Node predNode: nodePreset) {
                LinkedHashSet<N> predNodePostset = postsets.get(predNode);
                if (predNodePostset != null) {
                    predNodePostset.remove(node);
                }
            }
        }
        removeHashes(node);

        if (node instanceof Connection connection) {
            Node first = connection.getFirst();
            Node second = connection.getSecond();

            LinkedHashSet<N> firstPostset = postsets.get(first);
            if (firstPostset != null) {
                firstPostset.remove(second);
            }
            LinkedHashSet<N> secondPreset = presets.get(second);
            if (secondPreset != null) {
                secondPreset.remove(first);
            }
            LinkedHashSet<C> firstConnections = connections.get(first);
            if (firstConnections != null) {
                firstConnections.remove(connection);
            }
            LinkedHashSet<C> secondConnections = connections.get(second);
            if (secondConnections != null) {
                secondConnections.remove(connection);
            }
        }

        Collection<Node> children = node.getChildren();
        if (children != null) {
            for (Node childNode : children) {
                nodeRemoved(childNode);
            }
        }
    }

    @Override
    public Set<N> getPreset(N node) {
        return Collections.unmodifiableSet(presets.get(node));
    }

    @Override
    public Set<N> getPostset(N node) {
        return Collections.unmodifiableSet(postsets.get(node));
    }

    @Override
    public Set<C> getConnections(Node node) {
        Set<C> ret = connections.get(node);
        return Collections.unmodifiableSet(ret == null ? new HashSet<>() : ret);
    }

    @Override
    public boolean hasConnection(N first, N second) {
        LinkedHashSet<N> firstPostset = postsets.get(first);
        return (firstPostset != null) && firstPostset.contains(second);
    }

    @Override
    public C getConnection(N first, N second) {
        for (C connection : getConnections(first)) {
            if ((connection.getFirst() == first) && (connection.getSecond() == second)) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesReparentingEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesAddedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeAdded(n);
            }
        } else if (e instanceof NodesDeletedEvent) {
            for (Node n : e.getAffectedNodes()) {
                nodeRemoved(n);
            }
        }
    }

}
