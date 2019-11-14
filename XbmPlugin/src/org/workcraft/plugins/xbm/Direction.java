package org.workcraft.plugins.xbm;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.xbm.utils.SignalPropertyUtil;

public enum Direction {

    PLUS(SignalPropertyUtil.DIRECTION_RISING),
    MINUS(SignalPropertyUtil.DIRECTION_FALLING),
    STABLE(SignalPropertyUtil.DIRECTION_STABLE),
    UNSTABLE(SignalPropertyUtil.DIRECTION_DDC_UNDETERMINED),
    EVENTUALLY_HIGH(SignalPropertyUtil.DIRECTION_DDC_RISING),
    EVENTUALLY_LOW(SignalPropertyUtil.DIRECTION_DDC_FALLING);

    private final String name;

    Direction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public Direction toggle() {
        switch (this) {
            case MINUS:
                return Direction.PLUS;
            case PLUS:
                return Direction.MINUS;
            default:
                return this;
        }
    }

    public static Direction convertFromString(String value) {
        switch (value) {
        case SignalPropertyUtil.DIRECTION_RISING:
            return PLUS;
        case SignalPropertyUtil.DIRECTION_FALLING:
            return MINUS;
        case SignalPropertyUtil.DIRECTION_STABLE:
            return STABLE;
        case SignalPropertyUtil.DIRECTION_DDC_UNDETERMINED:
            return UNSTABLE;
        case SignalPropertyUtil.DIRECTION_DDC_RISING:
            return EVENTUALLY_HIGH;
        case SignalPropertyUtil.DIRECTION_DDC_FALLING:
            return EVENTUALLY_LOW;
        default:
            throw new ArgumentException("An unknown direction was set for the signal.");
        }
    }

    public static String getPostfix(Direction value) {
        switch (value) {
            case PLUS:
                return SignalPropertyUtil.POSTFIX_RISING;
            case MINUS:
                return SignalPropertyUtil.POSTFIX_FALLING;
            case STABLE:
                return SignalPropertyUtil.POSTFIX_STABLE;
            case UNSTABLE:
                return SignalPropertyUtil.POSTFIX_DDC_UNDETERMINED;
            case EVENTUALLY_HIGH:
                return SignalPropertyUtil.POSTFIX_DDC_RISING;
            case EVENTUALLY_LOW:
                return SignalPropertyUtil.POSTFIX_DDC_FALLING;
            default:
                throw new RuntimeException("An unknown direction was found.");
        }
    }
}
