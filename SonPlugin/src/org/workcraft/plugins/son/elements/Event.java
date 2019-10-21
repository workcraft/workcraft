package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.son.util.Interval;

import java.awt.*;

@IdentifierPrefix("e")
@VisualClass (org.workcraft.plugins.son.elements.VisualEvent.class)
public class Event extends MathNode implements TransitionNode, Time {

    private Color foregroundColor = VisualCommonSettings.getBorderColor();
    private Color fillColor = VisualCommonSettings.getFillColor();
    private String label = "";
    private Boolean faulty = false;

    private Interval statTime = new Interval(0, 9999);
    private Interval endTime = new Interval(0, 9999);
    private Interval duration = new Interval(0, 0);

    @Override
    public void setLabel(String value) {
        if (value == null) value = "";
        if (!value.equals(value)) {
            label = value;
            sendNotification(new PropertyChangedEvent(this, "label"));
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColor;
    }

    @Override
    public void setFaulty(boolean value) {
        if (faulty != value) {
            faulty = value;
            sendNotification(new PropertyChangedEvent(this, "fault"));
        }
    }

    @Override
    public boolean isFaulty() {
        return faulty;
    }

    @Override
    public void setStartTime(Interval value) {
        if (endTime != value) {
            statTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_START_TIME));
        }
    }

    @Override
    public Interval getStartTime() {
        return statTime;
    }

    @Override
    public void setEndTime(Interval value) {
        if (endTime != value) {
            endTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_END_TIME));
        }
    }

    @Override
    public Interval getEndTime() {
        return endTime;
    }

    @Override
    public void setDuration(Interval value) {
        if (duration != value) {
            duration = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_DURATION));
        }
    }

    @Override
    public Interval getDuration() {
        return duration;
    }

    @Override
    public void setForegroundColor(Color value) {
        if (!foregroundColor.equals(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
        }
    }

    @Override
    public void setFillColor(Color value) {
        if (!fillColor.equals(value)) {
            fillColor = value;
            sendNotification(new PropertyChangedEvent(this, "fillColor"));
        }
    }

    @Override
    public Color getFillColor() {
        return fillColor;
    }

}
