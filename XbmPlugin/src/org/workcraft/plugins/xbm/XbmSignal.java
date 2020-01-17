package org.workcraft.plugins.xbm;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

import java.util.regex.Pattern;

@IdentifierPrefix("x")
public class XbmSignal extends MathNode {

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_TYPE = "Type";
    public static final Pattern VALID_SIGNAL_NAME = Pattern.compile("^\\w+$");
    public static final Type DEFAULT_SIGNAL_TYPE = Type.INPUT;

    public enum Type {
        INPUT("input"),
        OUTPUT("output"),
        CONDITIONAL("conditional");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Type toggle() {
            switch (this) {
            case INPUT: return CONDITIONAL;
            case OUTPUT: return INPUT;
            case CONDITIONAL: return OUTPUT;
            default: return this;
            }
        }
    }

    private String name;
    private Type type;

    public XbmSignal() {
        this("", DEFAULT_SIGNAL_TYPE);
    }

    public XbmSignal(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        if (!name.equals(value)) {
            name = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_NAME));
        }
    }

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }
}
