package org.workcraft.plugins.cpog;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.BooleanOperations;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.cpog.VisualRhoClause.class)
public class RhoClause extends MathNode {
    public static final String PROPERTY_FORMULA = "Formula";

    private BooleanFormula formula = BooleanOperations.ONE;

    public void setFormula(BooleanFormula value) {
        if (formula != value) {
            formula = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FORMULA));
        }
    }

    public BooleanFormula getFormula() {
        return formula;
    }

}
