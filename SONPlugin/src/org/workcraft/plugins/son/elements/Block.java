package org.workcraft.plugins.son.elements;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.propertydescriptors.DurationPropertyDescriptor;
import org.workcraft.plugins.son.propertydescriptors.EndTimePropertyDescriptor;
import org.workcraft.plugins.son.propertydescriptors.StartTimePropertyDescriptor;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.util.Hierarchy;

@VisualClass (org.workcraft.plugins.son.elements.VisualBlock.class)
public class Block extends PageNode implements TransitionNode, Time{
    private String label="";
    private Color foregroundColor = CommonVisualSettings.getBorderColor();
    private Color fillColor  = CommonVisualSettings.getFillColor();
    private boolean isCollapsed = false;

    private Interval duration = new Interval(0000, 9999);
    private Interval statTime = new Interval(0000, 9999);
    private Interval endTime = new Interval(0000, 9999);

    private Color durationColor = Color.BLACK;

    public Collection<Node> getComponents(){
        ArrayList<Node> result = new ArrayList<Node>();
        result.addAll(getConditions());
        result.addAll(getEvents());
        return result;
    }

    public void setIsCollapsed(boolean isCollapsed) {
        this.isCollapsed = isCollapsed;
        sendNotification( new PropertyChangedEvent(this, "Is collapsed") );
    }

    public Collection<Condition> getConditions(){
        return Hierarchy.getDescendantsOfType(this, Condition.class);
    }

    public Collection<Event> getEvents(){
        return Hierarchy.getDescendantsOfType(this, Event.class);
    }

    public Collection<PageNode> getPageNodes(){
        return Hierarchy.getDescendantsOfType(this, PageNode.class);
    }

    public Collection<Block> getBlock(){
        return Hierarchy.getDescendantsOfType(this, Block.class);
    }

    public Collection<SONConnection> getSONConnections(){
        return Hierarchy.getDescendantsOfType(this, SONConnection.class);
    }

    @Override
    public boolean isFaulty(){
        for(Event event : getEvents())
            if(event.isFaulty())
                return true;
        return false;
    }

    @Override
    public void setForegroundColor(Color color){
        this.foregroundColor = color;
        sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
    }

    @Override
    public Color getForegroundColor(){
        return foregroundColor;
    }

    @Override
    public void setFillColor(Color color){
        this.fillColor = color;
        sendNotification(new PropertyChangedEvent(this, "fillColor"));
    }

    @Override
    public Color getFillColor(){
        return fillColor;
    }

    public boolean getIsCollapsed() {
        return isCollapsed;
    }

    @Override
    public void setLabel(String label){
        this.label = label;
        sendNotification(new PropertyChangedEvent(this, "label"));
    }

    @Override
    public String getLabel(){
        return label;
    }

    @Override
    public void setFaulty(boolean fault) {
    }

    public void setDuration(Interval duration){
        this.duration = duration;
        sendNotification( new PropertyChangedEvent(this, DurationPropertyDescriptor.PROPERTY_DURATION) );
    }

    @Override
    public Interval getDuration(){
        return duration;
    }

    public Color getDurationColor(){
        return durationColor;
    }

    public void setDurationColor(Color value){
        this.durationColor = value;
    }

    @Override
    public void setStartTime(Interval duration){
        this.statTime = duration;
        sendNotification( new PropertyChangedEvent(this, StartTimePropertyDescriptor.PROPERTY_START_TIME) );
    }

    @Override
    public Interval getStartTime(){
        return statTime;
    }

    @Override
    public void setEndTime(Interval endTime){
        this.endTime = endTime;
        sendNotification( new PropertyChangedEvent(this, EndTimePropertyDescriptor.PROPERTY_END_TIME) );
    }

    @Override
    public Interval getEndTime(){
        return endTime;
    }
}
