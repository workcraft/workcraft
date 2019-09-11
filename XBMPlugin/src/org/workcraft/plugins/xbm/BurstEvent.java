package org.workcraft.plugins.xbm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.fsm.Event;
import org.workcraft.plugins.fsm.VisualEvent;

@DisplayName("Burst event")
@IdentifierPrefix(value = "e", isInternal = true)
@VisualClass(VisualBurstEvent.class)
public class BurstEvent extends Event {

    public final static String PROPERTY_CONDITIONAL = "Conditional";

    private Burst burst;
    private final Conditional conditional;

    public BurstEvent() {
        conditional = new Conditional();
    }

    public BurstEvent(XbmState first, XbmState second, Burst burst) {
        super(first, second, burst);
        this.burst = burst;
        conditional = new Conditional();
    }

    public Burst getBurst() {
        return burst;
    }

    public void setBurst(Burst burst) {
        this.burst = burst;
    }

    public Conditional getConditionalMapping() {
        return conditional;
    }

    public void setConditional(String value) {

        Conditional oldConditional = new Conditional();
        if (!conditional.isEmpty()) {
            oldConditional.putAll(conditional);
        }
        try {
            conditional.setConditional(value);
            sendNotification(new PropertyChangedEvent(this, PROPERTY_CONDITIONAL));
        }
        catch (RuntimeException rxe) { //Determine if an error is thrown by either
            conditional.clear();
            conditional.putAll(oldConditional);
            throw new ArgumentException(rxe.getMessage());
        }
    }

    public String getConditional() {
        return conditional.toString();
    }

    public boolean hasConditional() {
        return !conditional.isEmpty();
    }

    public void addOrChangeSignalDirection(XbmSignal s, Burst.Direction d) {
        burst.addOrChangeSignalDirection(s, d);
        sendNotification(new PropertyChangedEvent(this, Burst.PROPERTY_DIRECTION));
    }

    public String getAsString() {

        final Burst burst = getBurst();
        String result = "";
        if (hasConditional()) {
            result += "<" + getConditional() + "> ";
            if (!burst.getAsString().equals(VisualEvent.EPSILON_SYMBOL)) {
                result += burst.getAsString();
            }
            else {
                result += "/";
            }
        }
        else {
            result += burst.getAsString();
        }
        return result;
    }
}
