package org.workcraft.plugins.cpog;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.NamePropertyDescriptor;
import org.workcraft.plugins.cpog.observers.ConditionConsistencySupervisor;
import org.workcraft.util.Hierarchy;

import java.util.Collection;

public class Cpog extends AbstractMathModel {

    public Cpog() {
        this(null);
    }

    public Cpog(Container root) {
        super(root);
        new ConditionConsistencySupervisor(this).attach(getRoot());
    }

    public Arc connect(Vertex first, Vertex second) {
        Arc con = new Arc(first, second);
        getRoot().add(con);
        return con;
    }

    public DynamicVariableConnection connect(Vertex first, Variable second) throws InvalidConnectionException {
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

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node != null) {
            properties.removeByName(NamePropertyDescriptor.PROPERTY_NAME);
        }
        return properties;
    }

}
