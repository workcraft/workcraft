package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.PageNode;
import org.workcraft.observation.PropertyChangedEvent;

@DisplayName("Waveform")
@IdentifierPrefix("w")
@VisualClass(org.workcraft.plugins.wtg.VisualWaveform.class)
public class Waveform extends PageNode {

    public static final String PROPERTY_GUARD = "Guard";

    private final Guard guard = new Guard();

    public void setGuard(Guard value) {
        if ((value != null) && !value.equals(guard)) {
            guard.clear();
            guard.putAll(value);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_GUARD));
        }
    }

    public Guard getGuard() {
        return guard;
    }

}
