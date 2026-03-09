package org.workcraft.plugins.cflt.graph;

public record Vertex(String name, boolean isClone, Integer cloneGeneration) {

    public Vertex(String name) {
        this(name, false, null);
    }

    public Vertex clone(int cloneGeneration) {
        return new Vertex(this.name, true, cloneGeneration);
    }
}