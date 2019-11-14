package org.workcraft.plugins.xbm.utils;

public class SignalPropertyUtil {
    //Signal state properties
    public static final String VALUE_HIGH = "1";
    public static final String VALUE_LOW = "0";
    public static final String VALUE_DDC = "?";

    //Direction properties
    public static final String DIRECTION_RISING = "+";
    public static final String DIRECTION_FALLING = "-";
    public static final String DIRECTION_STABLE = "!";
    public static final String DIRECTION_DDC_UNDETERMINED = "*";
    public static final String DIRECTION_DDC_RISING = "#"; //Unstable signal for eventually high
    public static final String DIRECTION_DDC_FALLING = "~"; //Unstable signal for eventually low

    //Direction postfix
    public static final String POSTFIX_RISING = "(rising)";
    public static final String POSTFIX_FALLING = "(falling)";
    public static final String POSTFIX_STABLE = "(stable)";
    public static final String POSTFIX_DDC_UNDETERMINED = "(unstable)";
    public static final String POSTFIX_DDC_RISING = "(eventually high)";
    public static final String POSTFIX_DDC_FALLING = "(eventually low)";
}
