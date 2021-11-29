package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.util.LinkedList;
import java.util.List;

public class RandomLayoutSettings extends AbstractLayoutSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "RandomLayoutSettings";

    private static final String keyStartX = prefix + ".startX";
    private static final String keyStartY = prefix + ".startY";
    private static final String keyRangeX = prefix + ".rangeX";
    private static final String keyRangeY = prefix + ".rangeY";

    private static final double defaultStartX = -25;
    private static final double defaultStartY = -15;
    private static final double defaultRangeX = 50;
    private static final double defaultRangeY = 30;

    private static double startX = defaultStartX;
    private static double startY = defaultStartY;
    private static double rangeX = defaultRangeX;
    private static double rangeY = defaultRangeY;

    static {
        properties.add(new PropertyDeclaration<>(Double.class,
                "Start X",
                RandomLayoutSettings::setStartX,
                RandomLayoutSettings::getStartX));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Start Y",
                RandomLayoutSettings::setStartY,
                RandomLayoutSettings::getStartY));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Range X",
                RandomLayoutSettings::setRangeX,
                RandomLayoutSettings::getRangeX));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Range Y",
                RandomLayoutSettings::setRangeY,
                RandomLayoutSettings::getRangeY));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setStartX(config.getDouble(keyStartX, defaultStartX));
        setStartY(config.getDouble(keyStartY, defaultStartY));
        setRangeX(config.getDouble(keyRangeX, defaultRangeX));
        setRangeY(config.getDouble(keyRangeY, defaultRangeY));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyStartX, getStartX());
        config.setDouble(keyStartY, getStartY());
        config.setDouble(keyRangeX, getRangeX());
        config.setDouble(keyRangeY, getRangeY());
    }

    @Override
    public String getName() {
        return "Random";
    }

    public static double getStartX() {
        return startX;
    }

    public static void setStartX(double value) {
        startX = value;
    }

    public static double getStartY() {
        return startY;
    }

    public static void setStartY(double value) {
        startY = value;
    }

    public static double getRangeX() {
        return rangeX;
    }

    public static void setRangeX(double value) {
        rangeX = value;
    }

    public static double getRangeY() {
        return rangeY;
    }

    public static void setRangeY(double value) {
        rangeY = value;
    }

}
