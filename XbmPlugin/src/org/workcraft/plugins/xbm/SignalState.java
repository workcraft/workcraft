package org.workcraft.plugins.xbm;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.xbm.utils.SignalPropertyUtil;

public enum SignalState {

    HIGH(SignalPropertyUtil.VALUE_HIGH),
    LOW(SignalPropertyUtil.VALUE_LOW),
    DDC(SignalPropertyUtil.VALUE_DDC);

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
        switch (this) {
        case LOW:
            return HIGH;
        case HIGH:
            return LOW;
        default:
            return this;
        }
    }

    public static SignalState convertFromString(String value) {
        switch (value) {
        case SignalPropertyUtil.VALUE_LOW:
            return LOW;
        case SignalPropertyUtil.VALUE_HIGH:
            return HIGH;
        case SignalPropertyUtil.VALUE_DDC:
            return DDC;
        default:
            throw new ArgumentException("An unknown state was set for the signal");
        }
    }

    public Direction determineDirectionBetween(SignalState other) {
        if (this == null || other == null) {
            return null;
        } else {
            if (this == other) {
                return other == DDC ? Direction.UNSTABLE : Direction.STABLE;
            } else {
                switch (other) {
                case HIGH:
                    return Direction.PLUS;
                case LOW:
                    return Direction.MINUS;
                case DDC:
                    return Direction.UNSTABLE;
                default:
                    return null;
                }
            }
        }
    }
}
