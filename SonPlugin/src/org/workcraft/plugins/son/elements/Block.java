package org.workcraft.plugins.son.elements;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

@IdentifierPrefix("b")
@VisualClass (org.workcraft.plugins.son.elements.VisualBlock.class)
public class Block extends PageNode implements TransitionNode, Time {
    private String label = "";
    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor = CommonVisualSettings.getFillColor();
    private boolean isCollapsed = false;

    private Interval duration = new Interval(0, 9999);
    private Interval statTime = new Interval(0, 9999);
    private Interval endTime = new Interval(0, 9999);

    private Color durationColor = Color.BLACK;

    public Collection<MathNode> getComponents() {
        ArrayList<MathNode> result = new ArrayList<>();
        result.addAll(getConditions());
        result.addAll(getEvents());
        return result;
    }

    public void setIsCollapsed(boolean value) {
        if (isCollapsed != value) {
            isCollapsed = value;
            sendNotification(new PropertyChangedEvent(this, "Is collapsed"));
        }
    }

    public Collection<Condition> getConditions() {
        return Hierarchy.getDescendantsOfType(this, Condition.class);
    }

    public Collection<Event> getEvents() {
        return Hierarchy.getDescendantsOfType(this, Event.class);
    }

    public Collection<PageNode> getPageNodes() {
        return Hierarchy.getDescendantsOfType(this, PageNode.class);
    }

    public Collection<Block> getBlock() {
        return Hierarchy.getDescendantsOfType(this, Block.class);
    }

    public Collection<SONConnection> getSONConnections() {
        return Hierarchy.getDescendantsOfType(this, SONConnection.class);
    }

    @Override
    public boolean isFaulty() {
        for (Event event : getEvents()) {
            if (event.isFaulty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setForegroundColor(Color value) {
        if (!foregroundColor.equals(value)) {
            foregroundColor = value;
            sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
        }
    }

    @Override
    public Color getForegroundColor() {
        return foregroundColor;
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

    public boolean getIsCollapsed() {
        return isCollapsed;
    }

    @Override
    public void setLabel(String value) {
        if (value == null) value = "";
        if (!value.equals(label)) {
            label = value;
            sendNotification(new PropertyChangedEvent(this, "label"));
        }
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void setFaulty(boolean fault) {
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

    public Color getDurationColor() {
        return durationColor;
    }

    public void setDurationColor(Color value) {
        this.durationColor = value;
    }

    @Override
    public void setStartTime(Interval value) {
        if (statTime != value) {
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
}
