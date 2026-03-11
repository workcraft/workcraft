package org.workcraft.plugins.cflt.graph;

import java.util.Objects;

public record Edge(Vertex firstVertex, Vertex secondVertex) {

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Edge other)) {
            return false;
        }

        return (Objects.equals(firstVertex, other.firstVertex) && Objects.equals(secondVertex, other.secondVertex))
                || (Objects.equals(firstVertex, other.secondVertex) && Objects.equals(secondVertex, other.firstVertex));
    }

    @Override
    public int hashCode() {
        // Order-independent hash
        return Objects.hash(firstVertex) + Objects.hash(secondVertex);
    }
}