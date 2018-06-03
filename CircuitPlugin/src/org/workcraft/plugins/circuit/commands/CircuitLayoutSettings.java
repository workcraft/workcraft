package org.workcraft.plugins.circuit.commands;

import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CircuitLayoutSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CircuitLayout";

    private static final String keySpacingHorizontal = prefix + ".spacingHorizontal";
    private static final String keySpacingVertical = prefix + ".spacingVertical";
    private static final String keyMarginObstacle = prefix + ".marginObstacle";
    private static final String keyMarginObstacleBusy = prefix + ".marginObstacleBusy";
    private static final String keyMarginChannel = prefix + ".marginChannel";
    private static final String keySnappingMajor = prefix + ".snappingMajor";
    private static final String keySnappingMinor = prefix + ".snappingMinor";
    private static final String keyDebugRouting = prefix + ".debugRouting";

    private static final double defaultSpacingHorizontal = 5.0;
    private static final double defaultSpacingVertical = 2.0;
    private static final double defaultMarginObstacle = 1.0;
    private static final double defaultMarginObstacleBusy = 0.5;
    private static final double defaultMarginChannel = 0.2;
    private static final double defaultSnappingMajor = 1.0;
    private static final double defaultSnappingMinor = 0.5;
    private static final boolean defaultDebugRouting = false;

    private static double spacingHorizontal = defaultSpacingHorizontal;
    private static double spacingVertical = defaultSpacingVertical;
    private static double marginObstacle = defaultMarginObstacle;
    private static double marginObstacleBusy = defaultMarginObstacleBusy;
    private static double marginChannel = defaultMarginChannel;
    private static double snappingMajor = defaultSnappingMajor;
    private static double snappingMinor = defaultSnappingMinor;
    private static boolean debugRouting = defaultDebugRouting;

    public CircuitLayoutSettings() {
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Placement spacing horizontally", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSpacingHorizontal(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSpacingHorizontal();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Placement spacing vertically", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSpacingVertical(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSpacingVertical();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing margin for obstacles", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginObstacle(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginObstacle();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing margin for obstacles in busy locations", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginObstacleBusy(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginObstacleBusy();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing marging for wire channel", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginChannel(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginChannel();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing major snapping", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSnappingMajor(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSnappingMajor();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing minor snapping", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSnappingMinor(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSnappingMinor();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Boolean>(
                this, "Debug routing", Boolean.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Boolean value) {
                setDebugRouting(value);
            }
            protected Boolean getter(CircuitLayoutSettings object) {
                return getDebugRouting();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setSpacingHorizontal(config.getDouble(keySpacingHorizontal, defaultSpacingHorizontal));
        setSpacingVertical(config.getDouble(keySpacingVertical, defaultSpacingVertical));
        setMarginObstacle(config.getDouble(keyMarginObstacle, defaultMarginObstacle));
        setMarginObstacleBusy(config.getDouble(keyMarginObstacleBusy, defaultMarginObstacleBusy));
        setMarginChannel(config.getDouble(keyMarginChannel, defaultMarginChannel));
        setSnappingMajor(config.getDouble(keySnappingMajor, defaultSnappingMajor));
        setSnappingMinor(config.getDouble(keySnappingMinor, defaultSnappingMinor));
        setDebugRouting(config.getBoolean(keyDebugRouting, defaultDebugRouting));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keySpacingHorizontal, getSpacingHorizontal());
        config.setDouble(keySpacingVertical, getSpacingVertical());
        config.setDouble(keyMarginObstacle, getMarginObstacle());
        config.setDouble(keyMarginObstacleBusy, getMarginObstacleBusy());
        config.setDouble(keyMarginChannel, getMarginChannel());
        config.setDouble(keySnappingMajor, getSnappingMajor());
        config.setDouble(keySnappingMinor, getSnappingMinor());
        config.setBoolean(keyDebugRouting, getDebugRouting());
    }

    @Override
    public String getSection() {
        return "Layout";
    }

    @Override
    public String getName() {
        return "Circuit";
    }

    public static double getSpacingHorizontal() {
        return spacingHorizontal;
    }

    public static void setSpacingHorizontal(double value) {
        spacingHorizontal = value;
    }

    public static double getSpacingVertical() {
        return spacingVertical;
    }

    public static void setSpacingVertical(double value) {
        spacingVertical = value;
    }

    public static double getMarginObstacle() {
        return marginObstacle;
    }

    public static void setMarginObstacle(double value) {
        marginObstacle = value;
    }

    public static double getMarginObstacleBusy() {
        return marginObstacleBusy;
    }

    public static void setMarginObstacleBusy(double value) {
        marginObstacleBusy = value;
    }

    public static double getMarginChannel() {
        return marginChannel;
    }

    public static void setMarginChannel(double value) {
        marginChannel = value;
    }

    public static double getSnappingMajor() {
        return snappingMajor;
    }

    public static void setSnappingMajor(double value) {
        snappingMajor = value;
    }

    public static double getSnappingMinor() {
        return snappingMinor;
    }

    public static void setSnappingMinor(double value) {
        snappingMinor = value;
    }

    public static boolean getDebugRouting() {
        return debugRouting;
    }

    public static void setDebugRouting(boolean value) {
        debugRouting = value;
    }

}
