package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("c")
@VisualClass (org.workcraft.plugins.son.elements.VisualCondition.class)
public class Condition extends PlaceNode {

    private boolean initialState = false;
    private boolean finalState = false;

    protected Color startTimeColor = Color.BLACK;
    protected Color endTimeColor = Color.BLACK;

    public boolean isInitial() {
        return initialState;
    }

    public void setInitial(boolean value) {
        if (initialState != value) {
            initialState = value;
            sendNotification(new PropertyChangedEvent(this, "initial"));
        }
    }

    public boolean isFinal() {
        return finalState;
    }

    public void setFinal(boolean value) {
        if (finalState != value) {
            finalState = value;
            sendNotification(new PropertyChangedEvent(this, "final"));
        }
    }

    public Color getStartTimeColor() {
        return startTimeColor;
    }

    public void setStartTimeColor(Color value) {
        startTimeColor = value;
    }

    public Color getEndTimeColor() {
        return endTimeColor;
    }

    public void setEndTimeColor(Color value) {
        endTimeColor = value;
    }
}
