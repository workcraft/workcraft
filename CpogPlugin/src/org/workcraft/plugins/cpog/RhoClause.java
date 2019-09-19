package org.workcraft.plugins.cpog;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("rho")
@VisualClass(VisualRhoClause.class)
public class RhoClause extends MathNode {
    public static final String PROPERTY_FORMULA = "Formula";

    private BooleanFormula formula = One.getInstance();

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
