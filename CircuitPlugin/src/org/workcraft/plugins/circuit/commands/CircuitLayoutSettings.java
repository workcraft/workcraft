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
    private static final String keyMarginSegment = prefix + ".marginSegment";
    private static final String keySnapMajor = prefix + ".snapMajor";
    private static final String keySnapMinor = prefix + ".snapMinor";
    private static final String keyDebugRouting = prefix + ".debugRouting";

    private static final double defaultSpacingHorizontal = 5.0;
    private static final double defaultSpacingVertical = 2.0;
    private static final double defaultMarginObstacle = 1.0;
    private static final double defaultMarginObstacleBusy = 0.5;
    private static final double defaultMarginSegment = 0.1;
    private static final double defaultSnapMajor = 1.0;
    private static final double defaultSnapMinor = 0.5;
    private static final boolean defaultDebugRouting = false;

    private static double spacingHorizontal = defaultSpacingHorizontal;
    private static double spacingVertical = defaultSpacingVertical;
    private static double marginObstacle = defaultMarginObstacle;
    private static double marginObstacleBusy = defaultMarginObstacleBusy;
    private static double marginSegment = defaultMarginSegment;
    private static double snapMajor = defaultSnapMajor;
    private static double snapMinor = defaultSnapMinor;
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
                this, "Routing margins for obstacles", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginObstacle(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginObstacle();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing margins for obstacles in busy locations", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginObstacleBusy(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginObstacleBusy();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing margins for wires", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setMarginSegment(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getMarginSegment();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing major snapping", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSnapMajor(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSnapMajor();
            }
        });
        properties.add(new PropertyDeclaration<CircuitLayoutSettings, Double>(
                this, "Routing minor snapping", Double.class, true, false, false) {
            protected void setter(CircuitLayoutSettings object, Double value) {
                setSnapMinor(value);
            }
            protected Double getter(CircuitLayoutSettings object) {
                return getSnapMinor();
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
        setMarginSegment(config.getDouble(keyMarginSegment, defaultMarginSegment));
        setSnapMajor(config.getDouble(keySnapMajor, defaultSnapMajor));
        setSnapMinor(config.getDouble(keySnapMinor, defaultSnapMinor));
        setDebugRouting(config.getBoolean(keyDebugRouting, defaultDebugRouting));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keySpacingHorizontal, getSpacingHorizontal());
        config.setDouble(keySpacingVertical, getSpacingVertical());
        config.setDouble(keyMarginObstacle, getMarginObstacle());
        config.setDouble(keyMarginObstacleBusy, getMarginObstacleBusy());
        config.setDouble(keyMarginSegment, getMarginSegment());
        config.setDouble(keySnapMajor, getSnapMajor());
        config.setDouble(keySnapMinor, getSnapMinor());
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

    public static double getMarginSegment() {
        return marginSegment;
    }

    public static void setMarginSegment(double value) {
        marginSegment = value;
    }

    public static double getSnapMajor() {
        return snapMajor;
    }

    public static void setSnapMajor(double value) {
        snapMajor = value;
    }

    public static double getSnapMinor() {
        return snapMinor;
    }

    public static void setSnapMinor(double value) {
        snapMinor = value;
    }

    public static boolean getDebugRouting() {
        return debugRouting;
    }

    public static void setDebugRouting(boolean value) {
        debugRouting = value;
    }

}
