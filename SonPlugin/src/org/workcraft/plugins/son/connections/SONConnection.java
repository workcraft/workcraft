package org.workcraft.plugins.son.connections;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.son.elements.Time;
import org.workcraft.plugins.son.util.Interval;

import java.awt.*;

@IdentifierPrefix("con")
public class SONConnection extends MathConnection {

    private Interval time = new Interval(0000, 9999);
    private Color timeColor = Color.BLACK;
    private Color color = CommonVisualSettings.getBorderColor();

    public enum Semantics {
        PNLINE("Petri net connection"),
        SYNCLINE("Synchronous communication"),
        ASYNLINE("Asynchronous communication"),
        BHVLINE("Behavioural abstraction");

        private final String name;

        Semantics(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private Semantics semantics = Semantics.PNLINE;

    public SONConnection() {
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color value) {
        if (!color.equals(value)) {
            color = value;
            sendNotification(new PropertyChangedEvent(this, "color"));
        }
    }

    public SONConnection(MathNode first, MathNode second, Semantics semantics) {
        super(first, second);
        this.setSemantics(semantics);
    }

    public Semantics getSemantics() {
        return semantics;
    }

    public void setSemantics(Semantics value) {
        if (semantics != value) {
            semantics = value;
            sendNotification(new PropertyChangedEvent(this, "semantics"));
        }
    }

    public Interval getTime() {
        return time;
    }

    public void setTime(Interval value) {
        if (time != value) {
            time = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_CONNECTION_TIME));
        }
    }

    public Color getTimeLabelColor() {
        return timeColor;
    }

    public void setTimeLabelColor(Color value) {
        timeColor = value;
    }
}
