package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.xmas.components.VisualQueueComponent.class)
public class QueueComponent extends XmasComponent {
    public static final String PROPERTY_CAPACITY = "Capacity";
    public static final String PROPERTY_INIT = "Init";

    public int capacity = 2;
    public int init = 0;

    public void setCapacity(int value) {
        if (capacity != value) {
            capacity = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_CAPACITY));
        }
    }

    public int getCapacity() {
        return capacity;
    }

    public void setInit(int value) {
        if (init != value) {
            init = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_INIT));
        }
    }

    public int getInit() {
        return init;
    }

}
