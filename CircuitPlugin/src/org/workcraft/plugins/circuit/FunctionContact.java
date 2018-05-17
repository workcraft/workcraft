package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.utils.StringGenerator;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.circuit.VisualFunctionContact.class)
public class FunctionContact extends Contact {
    public static final String PROPERTY_SET_FUNCTION = "Set function";
    public static final String PROPERTY_RESET_FUNCTION = "Reset function";

    private BooleanFormula setFunction = null;
    private BooleanFormula resetFunction = null;

    public FunctionContact(IOType ioType) {
        super(ioType);
    }

    public FunctionContact() {
        super();
    }

    public BooleanFormula getSetFunction() {
        return setFunction;
    }

    public void setSetFunction(BooleanFormula value) {
        if (setFunction != value) {
            String setFunctionString = StringGenerator.toString(setFunction);
            String valueString = StringGenerator.toString(value);
            if (!setFunctionString.equals(valueString)) {
                setSetFunctionQuiet(value);
                sendNotification(new PropertyChangedEvent(this, PROPERTY_SET_FUNCTION));
            }
        }
    }

    public boolean setSetFunctionQuiet(BooleanFormula value) {
        if (setFunction != value) {
            setFunction = value;
            return true;
        }
        return false;
    }

    public BooleanFormula getResetFunction() {
        return resetFunction;
    }

    public void setResetFunction(BooleanFormula value) {
        if (resetFunction != value) {
            String resetFunctionString = StringGenerator.toString(resetFunction);
            String valueString = StringGenerator.toString(value);
            if (!resetFunctionString.equals(valueString)) {
                setResetFunctionQuiet(value);
                sendNotification(new PropertyChangedEvent(this, PROPERTY_RESET_FUNCTION));
            }
        }
    }

    public boolean setResetFunctionQuiet(BooleanFormula value) {
        if (resetFunction != value) {
            resetFunction = value;
            return true;
        }
        return false;
    }

}
