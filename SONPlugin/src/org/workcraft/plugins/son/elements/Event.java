package org.workcraft.plugins.son.elements;

import java.awt.Color;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.propertydescriptors.DurationPropertyDescriptor;
import org.workcraft.plugins.son.propertydescriptors.EndTimePropertyDescriptor;
import org.workcraft.plugins.son.propertydescriptors.StartTimePropertyDescriptor;
import org.workcraft.plugins.son.util.Interval;

@VisualClass (org.workcraft.plugins.son.elements.VisualEvent.class)
public class Event extends Transition implements TransitionNode, Time {

    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor = CommonVisualSettings.getFillColor();
    private String label = "";
    private Boolean faulty = false;

    private Interval statTime = new Interval(0000, 9999);
    private Interval endTime = new Interval(0000, 9999);
    private Interval duration = new Interval(0000, 0000);

    @Override
    public void setLabel(String label) {
        this.label = label;
        sendNotification(new PropertyChangedEvent(this, "label"));
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
    public void setFaulty(boolean fault) {
        this.faulty = fault;
        sendNotification(new PropertyChangedEvent(this, "fault"));
    }

    @Override
    public boolean isFaulty() {
        return faulty;
    }

    public void setStartTime(Interval duration) {
        this.statTime = duration;
        sendNotification(new PropertyChangedEvent(this, StartTimePropertyDescriptor.PROPERTY_START_TIME));
    }

    public Interval getStartTime() {
        return statTime;
    }

    public void setEndTime(Interval endTime) {
        this.endTime = endTime;
        sendNotification(new PropertyChangedEvent(this, EndTimePropertyDescriptor.PROPERTY_END_TIME));
    }

    public Interval getEndTime() {
        return endTime;
    }

    public void setDuration(Interval duration) {
        this.duration = duration;
        sendNotification(new PropertyChangedEvent(this, DurationPropertyDescriptor.PROPERTY_DURATION));
    }

    public Interval getDuration() {
        return duration;
    }

    @Override
    public void setForegroundColor(Color foregroundColor) {
        this.foregroundColor = foregroundColor;
        sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
    }

    @Override
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        sendNotification(new PropertyChangedEvent(this, "fillColor"));
    }

    @Override
    public Color getFillColor() {
        return fillColor;
    }
}
