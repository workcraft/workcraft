package org.workcraft.plugins.petri;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("p")
@VisualClass(VisualPlace.class)
public class Place extends MathNode {
    public static final String PROPERTY_CAPACITY = "Promised capacity";
    public static final String PROPERTY_TOKENS = "Tokens";

    protected int tokens = 0;
    protected int capacity = 1;

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int value) {
        if (tokens != value) {
            if (value < 0) {
                throw new ArgumentException("The number of tokens cannot be negative.");
            }
            if (value > capacity) {
                setCapacity(value);
            }
            this.tokens = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKENS));
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int value) {
        if (capacity != value) {
            if (value < 1) {
                throw new ArgumentException("Negative or zero capacity is not allowed.");
            }
            if (tokens > value) {
                throw new ArgumentException("The place capacity " + value + " is too small for the current number of tokens " + tokens + " .");
            }
            this.capacity = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_CAPACITY));
        }
    }

}
