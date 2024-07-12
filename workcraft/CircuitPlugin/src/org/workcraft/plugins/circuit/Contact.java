package org.workcraft.plugins.circuit;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.formula.BooleanVariable;
import org.workcraft.formula.visitors.BooleanVisitor;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(VisualContact.class)
public class Contact extends MathNode implements BooleanVariable {

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_IO_TYPE = "I/O type";
    public static final String PROPERTY_INIT_TO_ONE = "Init to one";
    public static final String PROPERTY_FORCED_INIT = "Forced init";
    public static final String PROPERTY_PATH_BREAKER = "Path breaker";

    public enum IOType {
        INPUT("Input"),
        OUTPUT("Output");

        private final String name;

        IOType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private String name = "";
    private IOType type = IOType.OUTPUT;
    private boolean initToOne = false;
    private boolean forcedInit = false;
    private boolean pathBreaker = false;

    public Contact() {
    }

    public Contact(IOType type) {
        super();
        this.type = type;
    }

    // FIXME: This setName method is only to enable accessing contact name via getName. Use setName of Circuit class to set all node names!
    public void setName(String value) {
        if (value == null) value = "";
        if (!value.equals(name)) {
            name = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME));
        }
    }

    public String getName() {
        return name;
    }

    public void setIOType(IOType value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IO_TYPE));
        }
    }

    public IOType getIOType() {
        return type;
    }

    public boolean getInitToOne() {
        return initToOne;
    }

    public void setInitToOne(boolean value) {
        if (initToOne != value) {
            initToOne = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INIT_TO_ONE));
        }
    }

    public boolean getForcedInit() {
        return forcedInit;
    }

    public void setForcedInit(boolean value) {
        if (forcedInit != value) {
            forcedInit = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_FORCED_INIT));
        }
    }

    public boolean getPathBreaker() {
        return pathBreaker;
    }

    public void setPathBreaker(boolean value) {
        if (pathBreaker != value) {
            pathBreaker = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_PATH_BREAKER));
        }
    }

    @Override
    public <T> T accept(BooleanVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getLabel() {
        return getName();
    }

    public boolean isInput() {
        return getIOType() == IOType.INPUT;
    }

    public boolean isOutput() {
        return getIOType() == IOType.OUTPUT;
    }

    public boolean isPin() {
        return getParent() instanceof CircuitComponent;
    }

    public boolean isPort() {
        return !isPin();
    }

    public boolean isEnvironmentPin() {
        return (getParent() instanceof CircuitComponent) && ((CircuitComponent) getParent()).getIsEnvironment();
    }

    public boolean isDriver() {
        return isInput() == isPort();
    }

    public boolean isDriven() {
        return isOutput() == isPort();
    }

    public boolean isForcedDriver() {
        return isDriver() && getForcedInit();
    }

    public boolean isZeroDelayPin() {
        return (getParent() instanceof FunctionComponent) && ((FunctionComponent) getParent()).getIsZeroDelay();
    }

    public boolean isZeroDelayDriver() {
        return isDriver() && isZeroDelayPin();
    }

    public boolean isAvoidInitPin() {
        return (getParent() instanceof FunctionComponent) && ((FunctionComponent) getParent()).getAvoidInit();
    }

}
