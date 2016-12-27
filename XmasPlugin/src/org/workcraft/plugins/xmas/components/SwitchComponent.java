package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.xmas.components.VisualSwitchComponent.class)
public class SwitchComponent extends XmasComponent {

    public static final String PROPERTY_TYPE = "Type";
    public static final String PROPERTY_VAL = "Val";

    public enum Type {
        TYPE_E(" == ");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public enum Val {
        VAL_0("0"),
        VAL_1("1");

        private final String name;

        Val(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public Type type = Type.TYPE_E;
    public Val val = Val.VAL_0;

    public void setType(Type value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }

    public Type getType() {
        return type;
    }

    public void setVal(Val value) {
        if (val != value) {
            val = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }

    public Val getVal() {
        return val;
    }
}
