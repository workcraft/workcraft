package org.workcraft.plugins.xbm;

import org.workcraft.exceptions.ArgumentException;

public enum SignalState {

    HIGH("1"),
    LOW("0"),
    DDC("?");

    private final String name;

    SignalState(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public SignalState getValue() {
        return this;
    }

    public SignalState toggle() {
        switch(this) {
            case LOW: return HIGH;
            case HIGH: return LOW;
            default: return this;
        }
    }

    public static SignalState convertFromString(String value) {
        if (value.equals(LOW.toString())) return LOW;
        else if (value.equals(HIGH.toString())) return HIGH;
        else if (value.equals(DDC.toString())) return DDC;
        else throw new ArgumentException("An unknown state was set for the signal.");
    }
}
