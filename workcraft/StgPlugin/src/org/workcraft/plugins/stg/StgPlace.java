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
    public static final String PROPERTY_MUTEX_PROTOCOL = "Mutex protocol";

    private boolean implicit = false;
    private boolean mutex = false;
    private Mutex.Protocol mutexProtocol = StgSettings.getMutexProtocol();

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

    public Mutex.Protocol getMutexProtocol() {
        return mutexProtocol;
    }

    public void setMutexProtocol(Mutex.Protocol value) {
        if ((mutexProtocol != value) && (value != null)) {
            mutexProtocol = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_MUTEX_PROTOCOL));
        }
    }

}