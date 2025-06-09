package org.workcraft.plugins.fst;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.SignalCommonSettings;
import org.workcraft.plugins.fsm.Symbol;

import java.awt.*;

@IdentifierPrefix("x")
public class Signal extends Symbol {

    public static final String PROPERTY_TYPE = "Type";

    public enum Type {
        INPUT("input"),
        OUTPUT("output"),
        INTERNAL("internal"),
        DUMMY("dummy");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public Type toggle() {
            return switch (this) {
                case INPUT -> OUTPUT;
                case OUTPUT -> INTERNAL;
                case INTERNAL -> INPUT;
                case DUMMY -> DUMMY;
            };
        }

        public Color getColor() {
            return switch (this) {
                case INPUT -> SignalCommonSettings.getInputColor();
                case OUTPUT -> SignalCommonSettings.getOutputColor();
                case INTERNAL -> SignalCommonSettings.getInternalColor();
                default -> SignalCommonSettings.getDummyColor();
            };
        }
    }

    private Type type = Type.DUMMY;

    public Type getType() {
        return type;
    }

    public void setType(Type value) {
        if (type != value) {
            type = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TYPE));
        }
    }

    public boolean hasDirection() {
        return getType() != Type.DUMMY;
    }

}

