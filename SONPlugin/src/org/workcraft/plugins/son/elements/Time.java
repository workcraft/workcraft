package org.workcraft.plugins.son.elements;


import org.workcraft.dom.Node;
import org.workcraft.plugins.son.util.Interval;

public interface Time extends Node{
    void setStartTime(Interval value);
    Interval getStartTime();

    void setEndTime(Interval value);
    Interval getEndTime();

    void setDuration(Interval value);
    Interval getDuration();
}
