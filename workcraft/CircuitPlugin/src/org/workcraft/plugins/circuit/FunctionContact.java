package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.visitors.StringGenerator;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(VisualFunctionContact.class)
public class FunctionContact extends Contact {
    public static final String PROPERTY_SET_FUNCTION = "Set function";
    public static final String PROPERTY_RESET_FUNCTION = "Reset function";
    // Use for notification when either Set or Reset function is changed
    public static final String PROPERTY_FUNCTION = "Function";

    private BooleanFormula setFunction = null;
    private BooleanFormula resetFunction = null;

    public FunctionContact(IOType ioType) {
        super(ioType);
    }

    public FunctionContact() {
        super();
    }

    public void setBothFunctions(BooleanFormula newSetFunction, BooleanFormula newResetFunction) {
        boolean updated = false;
        if (setFunction != newSetFunction) {
            setSetFunctionQuiet(newSetFunction);
            updated = true;
        }
        if (resetFunction != newResetFunction) {
            setResetFunctionQuiet(newResetFunction);
            updated = true;
        }
        if (updated) {
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FUNCTION));
        }
    }
    public BooleanFormula getSetFunction() {
        return setFunction;
    }

    public void setSetFunction(BooleanFormula value) {
        if ((setFunction != value) && !StringGenerator.toString(setFunction).equals(StringGenerator.toString(value))) {
            setSetFunctionQuiet(value);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FUNCTION));
        }
    }

    public void setSetFunctionQuiet(BooleanFormula value) {
        setFunction = value;
    }

    public BooleanFormula getResetFunction() {
        return resetFunction;
    }

    public void setResetFunction(BooleanFormula value) {
        if ((resetFunction != value) && !StringGenerator.toString(resetFunction).equals(StringGenerator.toString(value))) {
            setResetFunctionQuiet(value);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FUNCTION));
        }
    }

    public void setResetFunctionQuiet(BooleanFormula value) {
        resetFunction = value;
    }

    public boolean hasFunction() {
        return (setFunction != null) || (resetFunction != null);
    }

    public boolean isSequential() {
        return (setFunction != null) && (resetFunction != null);
    }

}
