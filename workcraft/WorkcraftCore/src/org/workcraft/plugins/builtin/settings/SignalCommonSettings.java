package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class SignalCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonSignalSettings";

    private static final String keyInputColor = prefix + ".inputColor";
    private static final String keyOutputColor = prefix + ".outputColor";
    private static final String keyInternalColor = prefix + ".internalColor";
    private static final String keyDummyColor = prefix + ".dummyColor";
    private static final String keyShowToggle = prefix + ".showToggle";
    private static final String keyGroupByType = prefix + ".groupByType";

    private static final Color defaultInputColor = Color.RED.darker();
    private static final Color defaultOutputColor = Color.BLUE.darker();
    private static final Color defaultInternalColor = Color.GREEN.darker();
    private static final Color defaultDummyColor = Color.BLACK.darker();
    private static final boolean defaultShowToggle = false;
    private static final boolean defaultGroupByType = false;

    private static Color inputColor = defaultInputColor;
    private static Color outputColor = defaultOutputColor;
    private static Color internalColor = defaultInternalColor;
    private static Color dummyColor = defaultDummyColor;
    private static boolean showToggle = defaultShowToggle;
    private static boolean groupByType = defaultGroupByType;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Input signal color",
                SignalCommonSettings::setInputColor,
                SignalCommonSettings::getInputColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Output signal color",
                SignalCommonSettings::setOutputColor,
                SignalCommonSettings::getOutputColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Internal signal color",
                SignalCommonSettings::setInternalColor,
                SignalCommonSettings::getInternalColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Dummy color",
                SignalCommonSettings::setDummyColor,
                SignalCommonSettings::getDummyColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show signal toggle (~)",
                SignalCommonSettings::setShowToggle,
                SignalCommonSettings::getShowToggle));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show signal toggle",
                SignalCommonSettings::setGroupByType,
                SignalCommonSettings::getGroupByType));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setInputColor(config.getColor(keyInputColor, defaultInputColor));
        setOutputColor(config.getColor(keyOutputColor, defaultOutputColor));
        setInternalColor(config.getColor(keyInternalColor, defaultInternalColor));
        setDummyColor(config.getColor(keyDummyColor, defaultDummyColor));
        setShowToggle(config.getBoolean(keyShowToggle, defaultShowToggle));
        setGroupByType(config.getBoolean(keyGroupByType, defaultGroupByType));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyInputColor, getInputColor());
        config.setColor(keyOutputColor, getOutputColor());
        config.setColor(keyInternalColor, getInternalColor());
        config.setColor(keyDummyColor, getDummyColor());
        config.setBoolean(keyShowToggle, getShowToggle());
        config.setBoolean(keyGroupByType, getGroupByType());
    }

    @Override
    public String getName() {
        return "Signal";
    }

    public static void setInputColor(Color value) {
        inputColor = value;
    }

    public static Color getInputColor() {
        return inputColor;
    }

    public static void setOutputColor(Color value) {
        outputColor = value;
    }

    public static Color getOutputColor() {
        return outputColor;
    }

    public static void setInternalColor(Color value) {
        internalColor = value;
    }

    public static Color getInternalColor() {
        return internalColor;
    }

    public static void setDummyColor(Color value) {
        dummyColor = value;
    }

    public static Color getDummyColor() {
        return dummyColor;
    }

    public static void setShowToggle(boolean value) {
        showToggle = value;
    }

    public static boolean getShowToggle() {
        return showToggle;
    }

    public static void setGroupByType(boolean value) {
        groupByType = value;
    }

    public static boolean getGroupByType() {
        return groupByType;
    }

}
