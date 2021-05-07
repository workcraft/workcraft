package org.workcraft.plugins.stg;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class StgSettings extends AbstractModelSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "StgSettings";

    private static final String keyDensityMapLevelLimit = prefix + ".densityMapLevelLimit";
    private static final String keyLowLevelSuffix = prefix + ".lowLevelSuffix";
    private static final String keyHighLevelSuffix = prefix + ".highLevelSuffix";
    private static final String keyGroupSignalConversion = prefix + ".groupSignalConversion";
    private static final String keyShowTransitionInstance = prefix + ".showTransitionInstance";
    private static final String keyMutexProtocol = prefix + ".mutexProtocol";
    private static final String keyTransitionFontSize = prefix + ".transitionFontSize";

    private static final int defaultDensityMapLevelLimit = 5;
    private static final String defaultLowLevelSuffix = "_LOW";
    private static final String defaultHighLevelSuffix = "_HIGH";
    private static final boolean defaultGroupSignalConversion = false;
    private static final boolean defaultShowTransitionInstance = false;
    private static final Mutex.Protocol defaultMutexProtocol = Mutex.Protocol.LATE;
    private static final double defaultTransitionFontSize = 0.75f;

    private static int densityMapLevelLimit = defaultDensityMapLevelLimit;
    private static String lowLevelSuffix = defaultLowLevelSuffix;
    private static String highLevelSuffix = defaultHighLevelSuffix;
    private static boolean groupSignalConversion = defaultGroupSignalConversion;
    private static boolean showTransitionInstance = defaultShowTransitionInstance;
    private static Mutex.Protocol mutexProtocol = defaultMutexProtocol;
    private static double transitionFontSize = defaultTransitionFontSize;

    static {
        properties.add(new PropertyDeclaration<>(Integer.class,
                "Maximum number of encoding core density map levels",
                StgSettings::setDensityMapLevelLimit,
                StgSettings::getDensityMapLevelLimit));

        properties.add(new PropertyDeclaration<>(String.class,
                "Signal low level suffix for conversion to STG",
                value -> setLevelSuffix(value, StgSettings::setLowLevelSuffix),
                StgSettings::getLowLevelSuffix));

        properties.add(new PropertyDeclaration<>(String.class,
                "Signal high level suffix for conversion to STG",
                value -> setLevelSuffix(value, StgSettings::setHighLevelSuffix),
                StgSettings::getHighLevelSuffix));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Group signal places and transitions on conversion to STG",
                StgSettings::setGroupSignalConversion,
                StgSettings::getGroupSignalConversion));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show transition instance property",
                StgSettings::setShowTransitionInstance,
                StgSettings::getShowTransitionInstance));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Transition font size (cm)",
                StgSettings::setTransitionFontSize,
                StgSettings::getTransitionFontSize));
    }

    private static void setLevelSuffix(String value, Consumer<String> setter) {
        for (int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if (!Character.isDigit(c) && !Character.isLetter(c) && (c != '_')) {
                DialogUtils.showError("Signal level suffix must only consist of letters, numbers and underscores.");
                return;
            }
        }
        if (value.length() < 2) {
            DialogUtils.showWarning("Short signal level suffix increases the risk of name clashing.\n"
                    + "Consider making it at least two characters long.");
        }
        setter.accept(value);
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setDensityMapLevelLimit(config.getInt(keyDensityMapLevelLimit, defaultDensityMapLevelLimit));
        setLowLevelSuffix(config.getString(keyLowLevelSuffix, defaultLowLevelSuffix));
        setHighLevelSuffix(config.getString(keyHighLevelSuffix, defaultHighLevelSuffix));
        setGroupSignalConversion(config.getBoolean(keyGroupSignalConversion, defaultGroupSignalConversion));
        setShowTransitionInstance(config.getBoolean(keyShowTransitionInstance, defaultShowTransitionInstance));
        setMutexProtocol(config.getEnum(keyMutexProtocol, Mutex.Protocol.class, defaultMutexProtocol));
        setTransitionFontSize(config.getDouble(keyTransitionFontSize, defaultTransitionFontSize));
    }

    @Override
    public void save(Config config) {
        config.setInt(keyDensityMapLevelLimit, getDensityMapLevelLimit());
        config.set(keyLowLevelSuffix, getLowLevelSuffix());
        config.set(keyHighLevelSuffix, getHighLevelSuffix());
        config.setBoolean(keyGroupSignalConversion, getGroupSignalConversion());
        config.setBoolean(keyShowTransitionInstance, getShowTransitionInstance());
        config.setEnum(keyMutexProtocol, StgSettings.getMutexProtocol());
        config.setDouble(keyTransitionFontSize, getTransitionFontSize());
    }

    @Override
    public String getName() {
        return "Signal Transition Graph";
    }

    public static int getDensityMapLevelLimit() {
        return densityMapLevelLimit;
    }

    public static void setDensityMapLevelLimit(int value) {
        densityMapLevelLimit = value;
    }

    public static String getLowLevelSuffix() {
        return lowLevelSuffix;
    }

    public static void setLowLevelSuffix(String value) {
        if (value.length() > 0) {
            lowLevelSuffix = value;
        }
    }

    public static String getHighLevelSuffix() {
        return highLevelSuffix;
    }

    public static void setHighLevelSuffix(String value) {
        if (value.length() > 0) {
            highLevelSuffix = value;
        }
    }

    public static boolean getGroupSignalConversion() {
        return groupSignalConversion;
    }

    public static void setGroupSignalConversion(boolean value) {
        groupSignalConversion = value;
    }

    public static boolean getShowTransitionInstance() {
        return showTransitionInstance;
    }

    public static void setShowTransitionInstance(boolean value) {
        showTransitionInstance = value;
    }

    public static Mutex.Protocol getMutexProtocol() {
        return mutexProtocol;
    }

    public static void setMutexProtocol(Mutex.Protocol value) {
        mutexProtocol = value;
    }

    public static double getTransitionFontSize() {
        return transitionFontSize;
    }

    public static void setTransitionFontSize(double value) {
        transitionFontSize = value;
    }

}
