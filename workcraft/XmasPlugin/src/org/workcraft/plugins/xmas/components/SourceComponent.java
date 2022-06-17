package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("src")
@VisualClass(VisualSourceComponent.class)
public class SourceComponent extends XmasComponent {

    public static final String PROPERTY_TYPE = "Type";
    public static final String PROPERTY_MODE = "Mode";

    public enum Mode {
        MODE_0("0"),
        MODE_1("1"),
        MODE_2("2");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum Type {
        TYPE_T("t"),
        TYPE_0("0"),
        TYPE_1("1");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public Mode mode = Mode.MODE_1;
    public Type type = Type.TYPE_T;

    public void setMode(Mode value) {
        if (mode != value) {
            mode = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_MODE));
        }
    }

    public Mode getMode() {
        return mode;
    }

    public void setType(Type value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }

    public Type getType() {
        return type;
    }

}
