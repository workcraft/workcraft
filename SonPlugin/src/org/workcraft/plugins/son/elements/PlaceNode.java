package org.workcraft.plugins.son.elements;

import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.util.Interval;

import java.awt.*;

public class PlaceNode extends MathNode implements Time {

    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor = CommonVisualSettings.getFillColor();
    private String label = "";
    private int errors = 0;

    private Interval startTime = new Interval(0000, 9999);
    private Interval endTime = new Interval(0000, 9999);
    private Interval duration = new Interval(0000, 9999);

    protected Color durationColor = Color.BLACK;

    private boolean marked = false;
    private Color tokenColor = CommonVisualSettings.getBorderColor();

    public void setMarked(boolean value) {
        if (marked != value) {
            marked = value;
            sendNotification(new PropertyChangedEvent(this, "marked"));
        }
    }

    public boolean isMarked() {
        return marked;
    }

    public void setErrors(int value) {
        if (errors != value) {
            errors = value;
            sendNotification(new PropertyChangedEvent(this, "errors"));
        }
    }

    public int getErrors() {
        return errors;
    }

    public Color getForegroundColor() {
        return foregroundColor;
    }

    public void setForegroundColor(Color value) {
        if (!foregroundColor.equals(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
        }
    }

    public void setFillColor(Color value) {
        if (!fillColor.equals(value)) {
            fillColor = value;
            sendNotification(new PropertyChangedEvent(this, "fillColor"));
        }
    }

    public Color getFillColor() {
        return fillColor;
    }

    public void setLabel(String value) {
        if (!label.equals(value)) {
            label = value;
            sendNotification(new PropertyChangedEvent(this, "label"));
        }
    }

    public String getLabel() {
        return label;
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color value) {
        if (!tokenColor.equals(value)) {
            tokenColor = value;
            sendNotification(new PropertyChangedEvent(this, "tokenColor"));
        }
    }

    public void setStartTime(Interval value) {
        if (startTime != value) {
            startTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_START_TIME));
        }
    }

    public Interval getStartTime() {
        return startTime;
    }

    public void setEndTime(Interval value) {
        if (endTime != value) {
            endTime = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_END_TIME));
        }
    }

    public Interval getEndTime() {
        return endTime;
    }

    public void setDuration(Interval value) {
        if (duration != value) {
            duration = value;
            sendNotification(new PropertyChangedEvent(this, Time.PROPERTY_DURATION));
        }
    }

    public Interval getDuration() {
        return duration;
    }

    public Color getDurationColor() {
        return durationColor;
    }

    public void setDurationColor(Color value) {
        this.durationColor = value;
    }
}
