package org.workcraft.plugins.cpog;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.cpog.VisualVertex.class)
public class Vertex extends MathNode {
    public static final String PROPERTY_CONDITION = "Condition";

    private BooleanFormula condition = One.instance();

    public void setCondition(BooleanFormula value) {
        if (condition != value) {
            condition = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_CONDITION));
        }
    }

    public BooleanFormula getCondition() {
        return condition;
    }

}
