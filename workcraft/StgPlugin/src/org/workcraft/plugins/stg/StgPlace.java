package org.workcraft.plugins.stg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.petri.Place;

@DisplayName("Place")
@IdentifierPrefix("p")
@VisualClass(VisualStgPlace.class)
public class StgPlace extends Place {
    public static final String PROPERTY_IMPLICIT = "Implicit";
    public static final String PROPERTY_MUTEX = "Mutex";

    private boolean implicit = false;
    private boolean mutex = false;

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(boolean value) {
        if (implicit != value) {
            implicit = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IMPLICIT));
        }
    }

    public boolean isMutex() {
        return mutex;
    }

    public void setMutex(boolean value) {
        if (mutex != value) {
            mutex = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_MUTEX));
        }
    }

}