package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.stg.references.StgNameManager;
import org.workcraft.serialisation.NoAutoSerialisation;

@DisplayName("Signal transition")
@IdentifierPrefix(StgNameManager.INTERNAL_SIGNAL_PREFIX)
@VisualClass(VisualSignalTransition.class)
public class SignalTransition extends NamedTransition {

    public static final String PROPERTY_SIGNAL_TYPE = "Signal type";
    public static final String PROPERTY_SIGNAL_NAME = "Signal name";
    public static final String PROPERTY_DIRECTION = "Direction";


    public enum Direction {
        PLUS("+"),
        MINUS("-"),
        TOGGLE("~");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Direction mirror() {
            return switch (this) {
                case PLUS -> MINUS;
                case MINUS -> PLUS;
                default -> this;
            };
        }

        public Direction toggle() {
            return switch (this) {
                case PLUS -> TOGGLE;
                case MINUS -> PLUS;
                case TOGGLE -> MINUS;
            };
        }
    }

    private Signal.Type type = Signal.Type.INTERNAL;
    private Direction direction = Direction.TOGGLE;
    private String signalName = "";

    public Signal.Type getSignalType() {
        return type;
    }

    public void setSignalType(Signal.Type value) {
        if ((type != value) && (value != null)) {
            setSignalTypeQuiet(value);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SIGNAL_TYPE));
        }
    }

    // FIXME: As signal type affects multiple transitions Stg.setSignalType instead!
    // This method is only to be used from Stg, StgNameManager, and SignalTypeConsistencySupervisor.
    public void setSignalTypeQuiet(Signal.Type value) {
        type = value;
    }

    @NoAutoSerialisation
    public Direction getDirection() {
        return direction;
    }

    // FIXME: As direction is part of the node reference use Stg.setDirection instead!
    // This method is only to be used from StgNameManager and SignalTransitionGeneratorTool.
    public void setDirectionQuiet(Direction value) {
        direction = value;
    }

    @NoAutoSerialisation
    public String getSignalName() {
        return signalName;
    }

    // FIXME: As signal name is part of the node reference use Stg.setName instead!
    // This method is only to be used from StgNameManager.
    public void setSignalNameQuiet(String value) {
        if (value == null) value = "";
        signalName = value;
    }

    @NoAutoSerialisation
    @Override
    public String getName() {
        return signalName + direction;
    }

}
