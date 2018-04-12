package org.workcraft.plugins.fst;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.State;

@DisplayName("Signal event")
@VisualClass(org.workcraft.plugins.fst.VisualSignalEvent.class)
public class SignalEvent extends Event {

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
    }

    private Direction direction = Direction.TOGGLE;

    public SignalEvent() {
    }

    public SignalEvent(State first, State second, Signal signal) {
        super(first, second, signal);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction value) {
        if (direction != value) {
            direction = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_DIRECTION));
        }
    }

    public Signal getSignal() {
        return (Signal) getSymbol();
    }

}
