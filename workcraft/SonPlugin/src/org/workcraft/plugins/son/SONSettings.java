package org.workcraft.plugins.son;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;

public class SONSettings extends AbstractModelSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "SONSettings";

    private static final String keyRelationErrColor = prefix + ".relationErrColor";
    private static final String keyCyclePathColor = prefix + ".cyclePathColor";
    private static final String keyConnectionErrColor = prefix + ".connectionErrColor";
    private static final String keyErrLabelColor = prefix + ".errLabelColor";

    private static final Color defaultRelationErrColor = new Color(255, 204, 204);
    private static final Color defaultCyclePathColor = new Color(255, 102, 102);
    private static final Color defaultConnectionErrColor = new  Color(255, 102, 102);
    private static final Color defaultErrLabelColor = VisualCommonSettings.getLabelColor();
    private static final Color defaultGroupForegroundColor = Color.GRAY;

    private static final boolean defaultTimeVisibility = false;
    private static final boolean defaultErrorTracing = false;

    private static Color relationErrColor = defaultRelationErrColor;
    private static Color cyclePathColor = defaultCyclePathColor;
    private static Color connectionErrColor = defaultConnectionErrColor;
    private static Color errLabelColor = defaultErrLabelColor;
    private static Color groupForegroundColor = defaultGroupForegroundColor;
    private static boolean timeVisibility = defaultTimeVisibility;
    private static boolean errorTracing = defaultErrorTracing;

    static {
        properties.add(new PropertyDeclaration<>(Color.class,
                "Erroneous node color(relation)",
                SONSettings::setRelationErrColor,
                SONSettings::getRelationErrColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Erroneous node color(cycle)",
                SONSettings::setCyclePathColor,
                SONSettings::getCyclePathColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Erroneous connection color",
                SONSettings::setConnectionErrColor,
                SONSettings::getConnectionErrColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Error label color",
                SONSettings::setErrLabelColor,
                SONSettings::getErrLabelColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Group Foreground color",
                SONSettings::setGroupForegroundColor,
                SONSettings::getGroupForegroundColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Block fill color",
                SONSettings::setGroupForegroundColor,
                SONSettings::getGroupForegroundColor));
    }

    @Override
    public Collection<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setRelationErrColor(config.getColor(keyRelationErrColor, defaultRelationErrColor));
        setCyclePathColor(config.getColor(keyCyclePathColor, defaultCyclePathColor));
        setConnectionErrColor(config.getColor(keyConnectionErrColor, defaultConnectionErrColor));
        setErrLabelColor(config.getColor(keyErrLabelColor, defaultErrLabelColor));
    }

    @Override
    public void save(Config config) {
        config.setColor(keyRelationErrColor, getRelationErrColor());
        config.setColor(keyCyclePathColor, getCyclePathColor());
        config.setColor(keyConnectionErrColor, getConnectionErrColor());
        config.setColor(keyErrLabelColor, getErrLabelColor());
    }

    @Override
    public String getName() {
        return "Structured Occurrence Nets";
    }

    public static void setRelationErrColor(Color value) {
        relationErrColor = value;
    }

    public static Color getRelationErrColor() {
        return relationErrColor;
    }

    public static void setCyclePathColor(Color value) {
        cyclePathColor = value;
    }

    public static Color getCyclePathColor() {
        return cyclePathColor;
    }

    public static void setConnectionErrColor(Color value) {
        connectionErrColor = value;
    }

    public static Color getConnectionErrColor() {
        return connectionErrColor;
    }

    public static Color getErrLabelColor() {
        return errLabelColor;
    }

    public static void setErrLabelColor(Color value) {
        errLabelColor = value;
    }

    public static Color getGroupForegroundColor() {
        return groupForegroundColor;
    }

    public static void setGroupForegroundColor(Color value) {
        groupForegroundColor = value;
    }

    public static Boolean getTimeVisibility() {
        return timeVisibility;
    }

    public static void setTimeVisibility(Boolean value) {
        timeVisibility = value;
    }

    public static boolean isErrorTracing() {
        return errorTracing;
    }

    public static void setErrorTracing(boolean value) {
        errorTracing = value;
    }

}
