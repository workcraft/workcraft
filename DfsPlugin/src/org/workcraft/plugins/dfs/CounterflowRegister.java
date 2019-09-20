package org.workcraft.plugins.dfs;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("r")
@VisualClass(VisualCounterflowRegister.class)
public class CounterflowRegister extends MathDelayNode {
    public static final String PROPERTY_AND_MARKED = "And-marked";
    public static final String PROPERTY_OR_MARKED = "Or-marked";
    private boolean orMarked = false;
    private boolean andMarked = false;

    public boolean isOrMarked() {
        return orMarked;
    }

    public void setOrMarked(boolean value) {
        if (orMarked != value) {
            orMarked = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_OR_MARKED));
        }
    }

    public boolean isAndMarked() {
        return andMarked;
    }

    public void setAndMarked(boolean value) {
        if (andMarked != value) {
            andMarked = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_AND_MARKED));
        }
    }

}
