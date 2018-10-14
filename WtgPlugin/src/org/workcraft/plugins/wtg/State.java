package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("State")
@IdentifierPrefix("s")
@VisualClass(org.workcraft.plugins.wtg.VisualState.class)
public class State extends MathNode {
    public static final String PROPERTY_INITIAL = "Initial";

    private boolean initialState = false;

    public boolean isInitial() {
        return initialState;
    }

    public void setInitial(boolean value) {
        if (setInitialQuiet(value)) {
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INITIAL));
        }
    }

    public boolean setInitialQuiet(boolean value) {
        if (initialState != value) {
            initialState = value;
            return true;
        }
        return false;
    }

}
