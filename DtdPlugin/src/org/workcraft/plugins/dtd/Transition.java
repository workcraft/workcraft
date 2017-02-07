package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.graph.Vertex;

@DisplayName("Transition")
@VisualClass(org.workcraft.plugins.dtd.VisualTransition.class)
public class Transition extends Vertex {

    public static final String PROPERTY_DIRECTION = "Direction";

    public enum Direction {
        RISE("+ (rise)"),
        FALL("- (fall)"),
        DESTABILISE("switch"),
        STABILISE("stabilise");

        private final String name;

        Direction(String name) {
            this.name = name;
        }

        public static Direction fromString(String s) {
            for (Direction item : Direction.values()) {
                if ((s != null) && (s.equals(item.name))) {
                    return item;
                }
            }
            throw new ArgumentException("Unexpected string: " + s);
        }

        @Override
        public String toString() {
            return name;
        }

        public Direction reverse() {
            switch (this) {
            case RISE: return FALL;
            case FALL: return RISE;
            case DESTABILISE: return STABILISE;
            case STABILISE: return DESTABILISE;
            }
            return null;
        }
    }

    private Direction direction = Direction.RISE;

    public Transition() {
    }

    public Transition(Signal signal) {
        super(signal);
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_DIRECTION));
    }

    public Signal getSignal() {
        return (Signal) getSymbol();
    }

    public State getNextState() {
        switch (getDirection()) {
        case RISE: return State.HIGH;
        case FALL: return State.LOW;
        case DESTABILISE:  return State.UNSTABLE;
        case STABILISE:  return State.STABLE;
        }
        return null;
    }

}
