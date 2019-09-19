package org.workcraft.plugins.dfs;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("l")
@VisualClass(VisualLogic.class)
public class Logic extends MathDelayNode {
    public static final String PROPERTY_EARLY_EVALUATION = "Early evaluation";
    public static final String PROPERTY_COMPUTED = "Computed";

    private boolean computed = false;
    private boolean earlyEvaluation = false;

    public boolean isComputed() {
        return computed;
    }

    public void setComputed(boolean value) {
        if (computed != value) {
            computed = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_COMPUTED));
        }
    }

    public boolean isEarlyEvaluation() {
        return earlyEvaluation;
    }

    public void setEarlyEvaluation(boolean value) {
        if (earlyEvaluation != value) {
            earlyEvaluation = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_EARLY_EVALUATION));
        }
    }

}
