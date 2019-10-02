package org.workcraft.plugins.xbm;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.PropertyChangedEvent;

import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

@Hotkey(KeyEvent.VK_S)
@DisplayName("XbmSignal")
public class XbmSignal extends MathNode {

    public static final String PROPERTY_NAME = "Name";
    public static final String PROPERTY_TYPE = "Type";
    public static final Pattern VALID_SIGNAL_NAME = Pattern.compile("^\\w+$");
    public static final Type DEFAULT_SIGNAL_TYPE = Type.DUMMY;

    public enum Type {
        INPUT("input"),
        OUTPUT("output"),
        DUMMY("dummy"),
        CONDITIONAL("conditional");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
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
