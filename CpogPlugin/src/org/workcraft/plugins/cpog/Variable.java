package org.workcraft.plugins.cpog;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.BooleanVisitor;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.cpog.VisualVariable.class)
public class Variable extends MathNode implements Comparable<Variable>, BooleanVariable {

    public static final String PROPERTY_STATE = "State";
    public static final String PROPERTY_LABEL = "Label";

    private VariableState state = VariableState.UNDEFINED;

    private String label = "";

    public void setState(VariableState state) {
        this.state = state;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_STATE));
    }

    public VariableState getState() {
        return state;
    }

    @Override
    public int compareTo(Variable o) {
        return label.compareTo(o.label);
    }

    public void setLabel(String label) {
        this.label = label;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_LABEL));
    }

    public String getLabel() {
        return label;
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
