package org.workcraft.plugins.layout;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.util.LinkedList;
import java.util.List;

public class RandomLayoutSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "RandomLayout";

    private static final String keyStartX = prefix + ".startX";
    private static final String keyStartY = prefix + ".startY";
    private static final String keyRangeX = prefix + ".rangeX";
    private static final String keyRangeY = prefix + ".rangeY";

    private static double defaultStartX = -25;
    private static double defaultStartY = -15;
    private static double defaultRangeX = 50;
    private static double defaultRangeY = 30;

    private static double startX = defaultStartX;
    private static double startY = defaultStartY;
    private static double rangeX = defaultRangeX;
    private static double rangeY = defaultRangeY;

    public RandomLayoutSettings() {
        properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
                this, "Start X", Double.class) {
            @Override
            public void setter(RandomLayoutSettings object, Double value) {
                setStartX(value);
            }
            @Override
            public Double getter(RandomLayoutSettings object) {
                return getStartX();
            }
        });

        properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
                this, "Start Y", Double.class) {
            @Override
            public void setter(RandomLayoutSettings object, Double value) {
                setStartY(value);
            }
            @Override
            public Double getter(RandomLayoutSettings object) {
                return getStartY();
            }
        });

        properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
                this, "Range X", Double.class) {
            @Override
            public void setter(RandomLayoutSettings object, Double value) {
                setRangeX(value);
            }
            @Override
            public Double getter(RandomLayoutSettings object) {
                return getRangeX();
            }
        });

        properties.add(new PropertyDeclaration<RandomLayoutSettings, Double>(
                this, "Range Y", Double.class) {
            @Override
            public void setter(RandomLayoutSettings object, Double value) {
                setRangeY(value);
            }
            @Override
            public Double getter(RandomLayoutSettings object) {
                return getRangeY();
            }
        });
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
    public String getSection() {
        return "Layout";
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
