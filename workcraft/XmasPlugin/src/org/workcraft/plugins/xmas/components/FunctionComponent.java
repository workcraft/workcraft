package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("fun")
@VisualClass(VisualFunctionComponent.class)
public class FunctionComponent extends XmasComponent {
    public static final String PROPERTY_TYPE = "Type";

    public enum Type {
        TYPE_T("t"),
        TYPE_NOT("!");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    };

    public Type type = Type.TYPE_T;

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
