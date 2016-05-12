package org.workcraft.plugins.dtd;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.graph.Symbol;

@DisplayName("Signal")
@VisualClass(org.workcraft.plugins.dtd.VisualSignal.class)
public class Signal extends Symbol {

    public static final String PROPERTY_TYPE = "Type";

    public enum Type {
        INPUT("input"),
        OUTPUT("output"),
        INTERNAL("internal");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        public static Type fromString(String s) {
            for (Type item : Type.values()) {
                if ((s != null) && (s.equals(item.name))) {
                    return item;
                }
            }
            throw new ArgumentException("Unexpected string: " + s);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private Type type = Type.OUTPUT;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
    }

}

