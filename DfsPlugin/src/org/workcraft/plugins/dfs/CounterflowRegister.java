package org.workcraft.plugins.dfs;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualCounterflowRegister.class)
public class CounterflowRegister extends MathDelayNode {
    public static final String PROPERTY_AND_MARKED = "And-marked";
    public static final String PROPERTY_OR_MARKED = "Or-marked";
    private boolean orMarked = false;
    private boolean andMarked = false;

    public boolean isOrMarked() {
        return orMarked;
    }

    public void setOrMarked(boolean orMarked) {
        this.orMarked = orMarked;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_OR_MARKED));
    }

    public boolean isAndMarked() {
        return andMarked;
    }

    public void setAndMarked(boolean andMarked) {
        this.andMarked = andMarked;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_AND_MARKED));
    }

}
