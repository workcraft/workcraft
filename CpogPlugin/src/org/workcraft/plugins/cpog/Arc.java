package org.workcraft.plugins.cpog;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.observation.PropertyChangedEvent;

public class Arc extends MathConnection {
    public static final String PROPERTY_CONDITION = "Condition";

    private BooleanFormula condition;

    public Arc() {
    }

    public Arc(Vertex first, Vertex second) {
        super(first, second);
        condition = One.instance();
    }

    public void setCondition(BooleanFormula condition) {
        this.condition = condition;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_CONDITION));
    }

    public BooleanFormula getCondition() {
        return condition;
    }
}
