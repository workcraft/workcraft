package org.workcraft.plugins.cpog;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.plugins.cpog.observers.ConditionConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.Collection;

public class Cpog extends AbstractMathModel {

    public Cpog() {
        this(null, null);
    }

    public Cpog(Container root, References refs) {
        super(root, refs);
        new ConditionConsistencySupervisor(this).attach(getRoot());
    }

    public Arc connect(Vertex first, Vertex second) {
        Arc con = new Arc(first, second);
        getRoot().add(con);
        return con;
    }

    public DynamicVariableConnection connect(Vertex first, Variable second) {
        DynamicVariableConnection con = new DynamicVariableConnection(first, second);
        getRoot().add(con);
        return con;
    }

    public Collection<Variable> getVariables() {
        return Hierarchy.getChildrenOfType(getRoot(), Variable.class);
    }

    public Collection<Vertex> getVertices() {
        return Hierarchy.getChildrenOfType(getRoot(), Vertex.class);
    }

    public Collection<Arc> getArcs() {
        return Hierarchy.getChildrenOfType(getRoot(), Arc.class);
    }

}
