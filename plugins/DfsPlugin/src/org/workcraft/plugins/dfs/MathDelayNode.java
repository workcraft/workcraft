package org.workcraft.plugins.dfs;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

public class MathDelayNode extends MathNode {
    public static final String PROPERTY_DELAY = "Delay";

    private double delay = 0.0;

    public double getDelay() {
        return delay;
    }

    public void setDelay(double value) {
        if (delay != value) {
            delay = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_DELAY));
        }
    }

}
