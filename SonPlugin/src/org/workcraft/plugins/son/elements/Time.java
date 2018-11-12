package org.workcraft.plugins.son.elements;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.util.Interval;

public interface Time extends Node {
    String PROPERTY_CONNECTION_TIME = "Time interval";
    String PROPERTY_START_TIME = "Start time";
    String PROPERTY_END_TIME = "End time";
    String PROPERTY_DURATION = "Duration";


    void setStartTime(Interval value);
    Interval getStartTime();

    void setEndTime(Interval value);
    Interval getEndTime();

    void setDuration(Interval value);
    Interval getDuration();
}
