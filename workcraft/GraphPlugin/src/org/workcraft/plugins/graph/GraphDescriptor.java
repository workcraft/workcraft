package org.workcraft.plugins.graph;

import org.workcraft.dom.ModelDescriptor;

public class GraphDescriptor implements ModelDescriptor {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public Graph createMathModel() {
        return new Graph();
    }

    @Override
    public VisualGraphDescriptor getVisualModelDescriptor() {
        return new VisualGraphDescriptor();
    }

    @Override
    public Rating getRating() {
        return Rating.TRIVIAL;
    }
}
