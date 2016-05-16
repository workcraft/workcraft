package org.workcraft.plugins.circuit;

import java.awt.Color;
import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CircuitSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CircuitSettings";

    private static final String keyShowContacts  = prefix + ".showContacts";
    private static final String keyShowZeroDelayNames = prefix + ".showZeroDelayNames";
    private static final String keyActiveWireColor  = prefix + ".activeWireColor";
    private static final String keyInactiveWireColor  = prefix + ".inactiveWireColor";
    private static final String keyInitialisedGateColor  = prefix + ".initialisedGateColor";
    private static final String keyConflictGateColor  = prefix + ".conflictGateColor";
    private static final String keyBorderWidth  = prefix + ".borderWidth";
    private static final String keyWireWidth  = prefix + ".wireWidth";
    private static final String keySimplifyStg = prefix + ".simplifyStg";
    private static final String keyGateLibrary = prefix + ".gateLibrary";
    private static final String keySubstitutionLibrary = prefix + ".substitutionLibrary";

    private static final boolean defaultShowContacts = false;
    private static final boolean defaultShowZeroDelayNames = false;
    private static final Color defaultActiveWireColor = new Color(1.0f, 0.0f, 0.0f);
    private static final Color defaultInactiveWireColor = new Color(0.0f, 0.0f, 1.0f);
    private static final Color defaultInitialisedGateColor = new Color(0.4f, 1.0f, 0.4f);
    private static final Color defaultConflictGateColor = new Color(1.0f, 0.4f, 0.0f);
    private static final Double defaultBorderWidth = 0.06;
    private static final Double defaultWireWidth = 0.04;
    private static final boolean defaultSimplifyStg = true;
    private static final String defaultGateLibrary = DesktopApi.getOs().isWindows() ? "libraries\\workcraft.lib" : "libraries/workcraft.lib";
    private static final String defaultSubstitutionLibrary = "";

    private static boolean showContacts = defaultShowContacts;
    private static boolean showZeroDelayNames = defaultShowZeroDelayNames;
    private static Color activeWireColor = defaultActiveWireColor;
    private static Color inactiveWireColor = defaultInactiveWireColor;
    private static Color initialisedGateColor = defaultInitialisedGateColor;
    private static Color conflictGateColor = defaultConflictGateColor;
    private static Double borderWidth = defaultBorderWidth;
    private static Double wireWidth = defaultWireWidth;
    private static boolean simplifyStg = defaultSimplifyStg;
    private static String gateLibrary = defaultGateLibrary;
    private static String substitutionLibrary = defaultSubstitutionLibrary;

    public CircuitSettings() {
        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Show contacts", Boolean.class, true, false, false) {
            protected void setter(CircuitSettings object, Boolean value) {
                setShowContacts(value);
            }
            protected Boolean getter(CircuitSettings object) {
                return getShowContacts();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Show names of zero-dealy components", Boolean.class, true, false, false) {
            protected void setter(CircuitSettings object, Boolean value) {
                setShowZeroDelayNames(value);
            }
            protected Boolean getter(CircuitSettings object) {
                return getShowZeroDelayNames();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Active wire", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setActiveWireColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getActiveWireColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Inactive wire", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setInactiveWireColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getInactiveWireColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Correctly initialised gate", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setInitialisedGateColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getInitialisedGateColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Incorrectly initialised gate", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setConflictGateColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getConflictGateColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Double>(
                this, "Border width", Double.class, true, false, false) {
            protected void setter(CircuitSettings object, Double value) {
                setBorderWidth(value);
            }
            protected Double getter(CircuitSettings object) {
                return getBorderWidth();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Double>(
                this, "Wire width", Double.class, true, false, false) {
            protected void setter(CircuitSettings object, Double value) {
                setWireWidth(value);
            }
            protected Double getter(CircuitSettings object) {
                return getWireWidth();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Simplify generated circuit STG", Boolean.class, true, false, false) {
            protected void setter(CircuitSettings object, Boolean value) {
                setSimplifyStg(value);
            }
            protected Boolean getter(CircuitSettings object) {
                return getSimplifyStg();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Gate library for technology mapping", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                setGateLibrary(value);
            }
            protected String getter(CircuitSettings object) {
                return getGateLibrary();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Substitution rules for export", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                setSubstitutionLibrary(value);
            }
            protected String getter(CircuitSettings object) {
                return getSubstitutionLibrary();
            }
        });
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getSection() {
        return "Models";
    }

    @Override
    public String getName() {
        return "Digital Circuit";
    }

    @Override
    public void load(Config config) {
        setShowContacts(config.getBoolean(keyShowContacts, defaultShowContacts));
        setShowZeroDelayNames(config.getBoolean(keyShowZeroDelayNames, defaultShowZeroDelayNames));
        setActiveWireColor(config.getColor(keyActiveWireColor, defaultActiveWireColor));
        setInactiveWireColor(config.getColor(keyInactiveWireColor, defaultInactiveWireColor));
        setInitialisedGateColor(config.getColor(keyInitialisedGateColor, defaultInitialisedGateColor));
        setConflictGateColor(config.getColor(keyConflictGateColor, defaultConflictGateColor));
        setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
        setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
        setSimplifyStg(config.getBoolean(keySimplifyStg, defaultSimplifyStg));
        setGateLibrary(config.getString(keyGateLibrary, defaultGateLibrary));
        setSubstitutionLibrary(config.getString(keySubstitutionLibrary, defaultSubstitutionLibrary));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowContacts, getShowContacts());
        config.setBoolean(keyShowZeroDelayNames, getShowZeroDelayNames());
        config.setColor(keyActiveWireColor, getActiveWireColor());
        config.setColor(keyInactiveWireColor, getInactiveWireColor());
        config.setColor(keyInitialisedGateColor, getInitialisedGateColor());
        config.setColor(keyConflictGateColor, getConflictGateColor());
        config.setDouble(keyBorderWidth, getBorderWidth());
        config.setDouble(keyWireWidth, getWireWidth());
        config.setBoolean(keySimplifyStg, getSimplifyStg());
        config.set(keyGateLibrary, getGateLibrary());
        config.set(keySubstitutionLibrary, getSubstitutionLibrary());
    }

    public static boolean getShowContacts() {
        return showContacts;
    }

    public static void setShowContacts(boolean value) {
        showContacts = value;
    }

    public static boolean getShowZeroDelayNames() {
        return showZeroDelayNames;
    }

    public static void setShowZeroDelayNames(boolean value) {
        showZeroDelayNames = value;
    }

    public static Color getActiveWireColor() {
        return activeWireColor;
    }

    public static void setActiveWireColor(Color value) {
        activeWireColor = value;
    }

    public static Color getInactiveWireColor() {
        return inactiveWireColor;
    }

    public static void setInactiveWireColor(Color value) {
        inactiveWireColor = value;
    }

    public static Color getInitialisedGateColor() {
        return initialisedGateColor;
    }

    public static void setInitialisedGateColor(Color value) {
        initialisedGateColor = value;
    }

    public static Color getConflictGateColor() {
        return conflictGateColor;
    }

    public static void setConflictGateColor(Color value) {
        conflictGateColor = value;
    }

    public static double getBorderWidth() {
        return borderWidth;
    }

    public static void setBorderWidth(double value) {
        borderWidth = value;
    }

    public static double getWireWidth() {
        return wireWidth;
    }

    public static void setWireWidth(double value) {
        wireWidth = value;
    }

    public static boolean getSimplifyStg() {
        return simplifyStg;
    }

    public static void setSimplifyStg(boolean value) {
        simplifyStg = value;
    }

    public static String getGateLibrary() {
        return gateLibrary;
    }

    public static void setGateLibrary(String value) {
        gateLibrary = value;
    }

    public static String getSubstitutionLibrary() {
        return substitutionLibrary;
    }

    public static void setSubstitutionLibrary(String value) {
        substitutionLibrary = value;
    }

}
