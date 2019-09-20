package org.workcraft.plugins.dfs;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("c")
@VisualClass(VisualControlRegister.class)
public class ControlRegister extends BinaryRegister {
    public static final String PROPERTY_PROBABILITY = "Probability";
    public static final String PROPERTY_SYNCHRONISATION_TYPE = "Synchronisation type";

    private SynchronisationType synchronisationType = SynchronisationType.PLAIN;
    private double probability = 1.0;

    public enum SynchronisationType {
        PLAIN("plain"),
        AND("and"),
        OR("or");

        private final String name;

        SynchronisationType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public SynchronisationType getSynchronisationType() {
        return synchronisationType;
    }

    public void setSynchronisationType(SynchronisationType value) {
        if (synchronisationType != value) {
            synchronisationType = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_SYNCHRONISATION_TYPE));
        }
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double value) {
        if (probability != value) {
            probability = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_PROBABILITY));
        }
    }

}
