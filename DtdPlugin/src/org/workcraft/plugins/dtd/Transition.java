package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.pog.Vertex;

@DisplayName("Transition")
@VisualClass(org.workcraft.plugins.dtd.VisualTransition.class)
public class Transition extends Vertex {

    public static final String PROPERTY_DIRECTION = "Direction";

    public enum Direction {
        PLUS("+"),
        MINUS("-");

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
            return (this == PLUS) ? MINUS : PLUS;
        }
    }

    private Direction direction = Direction.PLUS;

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

}
