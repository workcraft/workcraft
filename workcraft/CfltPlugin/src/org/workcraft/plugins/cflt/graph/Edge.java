package org.workcraft.plugins.cflt.graph;

public record Edge(Vertex firstVertex, Vertex secondVertex) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Edge other)) {
            return false;
        }

        return (firstVertex.equals(other.firstVertex) && secondVertex.equals(other.secondVertex))
                || (firstVertex.equals(other.secondVertex) && secondVertex.equals(other.firstVertex));
    }

    @Override
    public int hashCode() {
        // Order-independent hash
        return firstVertex.hashCode() + secondVertex.hashCode();
    }
}