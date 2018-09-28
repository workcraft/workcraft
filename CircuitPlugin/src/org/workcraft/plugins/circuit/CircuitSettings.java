package org.workcraft.plugins.circuit;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;
import org.workcraft.plugins.circuit.utils.Gate2;
import org.workcraft.plugins.circuit.utils.Gate3;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.util.DialogUtils;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CircuitSettings implements Settings {

    private static final Pattern MUTEX_DATA_PATTERN = Pattern.compile(
            "(\\w+)\\(\\((\\w+),(\\w+)\\),\\((\\w+),(\\w+)\\)\\)");

    private static final int MUTEX_NAME_GROUP = 1;
    private static final int MUTEX_R1_GROUP = 2;
    private static final int MUTEX_G1_GROUP = 3;
    private static final int MUTEX_R2_GROUP = 4;
    private static final int MUTEX_G2_GROUP = 5;

    private static final Pattern GATE2_DATA_PATTERN = Pattern.compile("(\\w+)\\((\\w+),(\\w+)\\)");
    private static final Pattern GATE3_DATA_PATTERN = Pattern.compile("(\\w+)\\((\\w+),(\\w+),(\\w+)\\)");
    private static final int GATE_NAME_GROUP = 1;
    private static final int GATE_PIN1_GROUP = 2;
    private static final int GATE_PIN2_GROUP = 3;
    private static final int GATE_PIN3_GROUP = 4;

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CircuitSettings";

    private static final String keyShowContacts = prefix + ".showContacts";
    private static final String keyShowZeroDelayNames = prefix + ".showZeroDelayNames";
    private static final String keyActiveWireColor = prefix + ".activeWireColor";
    private static final String keyInactiveWireColor = prefix + ".inactiveWireColor";
    private static final String keyConflictInitGateColor = prefix + ".conflictInitGateColor";
    private static final String keyForcedInitGateColor = prefix + ".forcedInitGateColor";
    private static final String keyPropagatedInitGateColor = prefix + ".propagatedInitGateColor";
    private static final String keyBorderWidth = prefix + ".borderWidth";
    private static final String keyWireWidth = prefix + ".wireWidth";
    private static final String keySimplifyStg = prefix + ".simplifyStg";
    private static final String keyGateLibrary = prefix + ".gateLibrary";
    private static final String keySubstitutionLibrary = prefix + ".substitutionLibrary";
    private static final String keyBufData = prefix + ".bufData";
    private static final String keyAndData = prefix + ".andData";
    private static final String keyOrData = prefix + ".orData";
    private static final String keyNandData = prefix + ".nandData";
    private static final String keyNorData = prefix + ".norData";
    private static final String keyNandbData = prefix + ".nandbData";
    private static final String keyNorbData = prefix + ".norbData";
    private static final String keyMutexData = prefix + ".mutexData";
    private static final String keyBusSuffix = prefix + ".busSuffix";
    private static final String keyResetName = prefix + ".resetName";

    private static final boolean defaultShowContacts = false;
    private static final boolean defaultShowZeroDelayNames = false;
    private static final Color defaultActiveWireColor = new Color(1.0f, 0.0f, 0.0f);
    private static final Color defaultInactiveWireColor = new Color(0.0f, 0.0f, 1.0f);
    private static final Color defaultConflictInitGateColor = new Color(1.0f, 0.4f, 1.0f);
    private static final Color defaultForcedInitGateColor = new Color(1.0f, 0.8f, 0.0f);
    private static final Color defaultPropagatedInitGateColor = new Color(0.4f, 1.0f, 0.4f);
    private static final Double defaultBorderWidth = 0.06;
    private static final Double defaultWireWidth = 0.04;
    private static final boolean defaultSimplifyStg = true;
    private static final String defaultGateLibrary = DesktopApi.getOs().isWindows() ? "libraries\\workcraft.lib" : "libraries/workcraft.lib";
    private static final String defaultSubstitutionLibrary = "";
    private static final String defaultBufData = "BUF (I, O)";
    private static final String defaultAndData = "AND2 (A, B, O)";
    private static final String defaultOrData = "OR2 (A, B, O)";
    private static final String defaultNandData = "NAND2 (A, B, ON)";
    private static final String defaultNorData = "NOR2 (A, B, ON)";
    private static final String defaultNandbData = "NAND2B (AN, B, ON)";
    private static final String defaultNorbData = "NOR2B (AN, B, ON)";
    private static final String defaultMutexData = "MUTEX ((r1, g1), (r2, g2))";
    private static final String defaultBusSuffix = "__$";
    private static final String defaultResetName = "reset";

    private static boolean showContacts = defaultShowContacts;
    private static boolean showZeroDelayNames = defaultShowZeroDelayNames;
    private static Color activeWireColor = defaultActiveWireColor;
    private static Color inactiveWireColor = defaultInactiveWireColor;
    private static Color conflictInitGateColor = defaultConflictInitGateColor;
    private static Color forcedInitGateColor = defaultForcedInitGateColor;
    private static Color propagatedInitGateColor = defaultPropagatedInitGateColor;
    private static Double borderWidth = defaultBorderWidth;
    private static Double wireWidth = defaultWireWidth;
    private static boolean simplifyStg = defaultSimplifyStg;
    private static String gateLibrary = defaultGateLibrary;
    private static String substitutionLibrary = defaultSubstitutionLibrary;
    private static String bufData = defaultBufData;
    private static String andData = defaultAndData;
    private static String orData = defaultOrData;
    private static String nandData = defaultNandData;
    private static String norData = defaultNorData;
    private static String nandbData = defaultNandbData;
    private static String norbData = defaultNorbData;
    private static String mutexData = defaultMutexData;
    private static String busSuffix = defaultBusSuffix;
    private static String resetName = defaultResetName;

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
                this, "Gate with conflict of initialisation", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setConflictInitGateColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getConflictInitGateColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Gate with forced initial state", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setForcedInitGateColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getForcedInitGateColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Gate with propagated initial state", Color.class, true, false, false) {
            protected void setter(CircuitSettings object, Color value) {
                setPropagatedInitGateColor(value);
            }
            protected Color getter(CircuitSettings object) {
                return getPropagatedInitGateColor();
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

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "BUF name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate2Data(value) != null) {
                    setBufData(value);
                } else {
                    DialogUtils.showError("BUF description format is incorrect. It should be as follows:\n" + defaultBufData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getBufData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "AND2 name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setAndData(value);
                } else {
                    DialogUtils.showError("AND2 description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getAndData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "OR2 name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setOrData(value);
                } else {
                    DialogUtils.showError("OR2 description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getOrData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NAND2 name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNandData(value);
                } else {
                    DialogUtils.showError("NAND2 description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getNandData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NOR2-gate name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNorData(value);
                } else {
                    DialogUtils.showError("NOR2 description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getNorData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NAND2B name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNandbData(value);
                } else {
                    DialogUtils.showError("NAND2B description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getNandbData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NOR2B-gate name and input-output pins", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNorbData(value);
                } else {
                    DialogUtils.showError("NOR2B description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getNorbData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Mutex name and request-grant pairs", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                if (parseMutexData(value) != null) {
                    setMutexData(value);
                } else {
                    DialogUtils.showError("Mutex description format is incorrect. It should be as follows:\n" + defaultMutexData);
                }
            }
            protected String getter(CircuitSettings object) {
                return getMutexData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Bus split suffix ($ is replaced by index)", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                setBusSuffix(value);
            }
            protected String getter(CircuitSettings object) {
                return getBusSuffix();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Reset port name", String.class, true, false, false) {
            protected void setter(CircuitSettings object, String value) {
                setResetName(value);
            }
            protected String getter(CircuitSettings object) {
                return getResetName();
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
        setConflictInitGateColor(config.getColor(keyConflictInitGateColor, defaultConflictInitGateColor));
        setForcedInitGateColor(config.getColor(keyForcedInitGateColor, defaultForcedInitGateColor));
        setPropagatedInitGateColor(config.getColor(keyPropagatedInitGateColor, defaultPropagatedInitGateColor));
        setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
        setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
        setSimplifyStg(config.getBoolean(keySimplifyStg, defaultSimplifyStg));
        setGateLibrary(config.getString(keyGateLibrary, defaultGateLibrary));
        setSubstitutionLibrary(config.getString(keySubstitutionLibrary, defaultSubstitutionLibrary));
        setBufData(config.getString(keyBufData, defaultBufData));
        setAndData(config.getString(keyAndData, defaultAndData));
        setOrData(config.getString(keyOrData, defaultOrData));
        setNandbData(config.getString(keyNandData, defaultNandData));
        setNorbData(config.getString(keyNorData, defaultNorData));
        setNandbData(config.getString(keyNandbData, defaultNandbData));
        setNorbData(config.getString(keyNorbData, defaultNorbData));
        setMutexData(config.getString(keyMutexData, defaultMutexData));
        setBusSuffix(config.getString(keyBusSuffix, defaultBusSuffix));
        setResetName(config.getString(keyResetName, defaultResetName));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowContacts, getShowContacts());
        config.setBoolean(keyShowZeroDelayNames, getShowZeroDelayNames());
        config.setColor(keyActiveWireColor, getActiveWireColor());
        config.setColor(keyInactiveWireColor, getInactiveWireColor());
        config.setColor(keyConflictInitGateColor, getConflictInitGateColor());
        config.setColor(keyForcedInitGateColor, getForcedInitGateColor());
        config.setColor(keyPropagatedInitGateColor, getPropagatedInitGateColor());
        config.setDouble(keyBorderWidth, getBorderWidth());
        config.setDouble(keyWireWidth, getWireWidth());
        config.setBoolean(keySimplifyStg, getSimplifyStg());
        config.set(keyGateLibrary, getGateLibrary());
        config.set(keySubstitutionLibrary, getSubstitutionLibrary());
        config.set(keyBufData, getBufData());
        config.set(keyAndData, getAndData());
        config.set(keyOrData, getOrData());
        config.set(keyNandData, getNandData());
        config.set(keyNorData, getNorData());
        config.set(keyNandbData, getNandbData());
        config.set(keyNorbData, getNorbData());
        config.set(keyMutexData, getMutexData());
        config.set(keyBusSuffix, getBusSuffix());
        config.set(keyResetName, getResetName());
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

    public static Color getConflictInitGateColor() {
        return conflictInitGateColor;
    }

    public static void setConflictInitGateColor(Color value) {
        conflictInitGateColor = value;
    }

    public static Color getForcedInitGateColor() {
        return forcedInitGateColor;
    }

    public static void setForcedInitGateColor(Color value) {
        forcedInitGateColor = value;
    }

    public static Color getPropagatedInitGateColor() {
        return propagatedInitGateColor;
    }

    public static void setPropagatedInitGateColor(Color value) {
        propagatedInitGateColor = value;
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

    public static String getBufData() {
        return bufData;
    }

    public static void setBufData(String value) {
        bufData = value;
    }

    public static Gate2 parseBufData() {
        return parseGate2Data(getBufData());
    }

    public static String getAndData() {
        return andData;
    }

    public static void setAndData(String value) {
        andData = value;
    }

    public static Gate3 parseAndData() {
        return parseGate3Data(getAndData());
    }

    public static String getOrData() {
        return orData;
    }

    public static void setOrData(String value) {
        orData = value;
    }

    public static Gate3 parseOrData() {
        return parseGate3Data(getOrData());
    }

    public static String getNandData() {
        return nandData;
    }

    public static void setNandData(String value) {
        nandData = value;
    }

    public static Gate3 parseNandData() {
        return parseGate3Data(getNandData());
    }

    public static String getNorData() {
        return norData;
    }

    public static void setNorData(String value) {
        norData = value;
    }

    public static Gate3 parseNorData() {
        return parseGate3Data(getNorData());
    }

    public static String getNandbData() {
        return nandbData;
    }

    public static void setNandbData(String value) {
        nandbData = value;
    }

    public static Gate3 parseNandbData() {
        return parseGate3Data(getNandbData());
    }

    public static String getNorbData() {
        return norbData;
    }

    public static void setNorbData(String value) {
        norbData = value;
    }

    public static Gate3 parseNorbData() {
        return parseGate3Data(getNorbData());
    }

    public static String getMutexData() {
        return mutexData;
    }

    public static void setMutexData(String value) {
        mutexData = value;
    }

    public static Mutex parseMutexData() {
        return parseMutexData(getMutexData());
    }

    public static String getBusSuffix() {
        return busSuffix;
    }

    public static void setBusSuffix(String value) {
        busSuffix = value;
    }

    public static String getResetName() {
        return resetName;
    }

    public static void setResetName(String value) {
        resetName = value;
    }

    private static Gate2 parseGate2Data(String str) {
        Gate2 result = null;
        Matcher matcher = GATE2_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(GATE_NAME_GROUP);
            String in = matcher.group(GATE_PIN1_GROUP);
            String out = matcher.group(GATE_PIN2_GROUP);
            result = new Gate2(name, in, out);
        }
        return result;
    }

    private static Gate3 parseGate3Data(String str) {
        Gate3 result = null;
        Matcher matcher = GATE3_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(GATE_NAME_GROUP);
            String in1 = matcher.group(GATE_PIN1_GROUP);
            String in2 = matcher.group(GATE_PIN2_GROUP);
            String out = matcher.group(GATE_PIN3_GROUP);
            result = new Gate3(name, in1, in2, out);
        }
        return result;
    }

    private static Mutex parseMutexData(String str) {
        Mutex result = null;
        Matcher matcher = MUTEX_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            Signal r1 = new Signal(matcher.group(MUTEX_R1_GROUP), Signal.Type.INPUT);
            Signal g1 = new Signal(matcher.group(MUTEX_G1_GROUP), Signal.Type.OUTPUT);
            Signal r2 = new Signal(matcher.group(MUTEX_R2_GROUP), Signal.Type.INPUT);
            Signal g2 = new Signal(matcher.group(MUTEX_G2_GROUP), Signal.Type.OUTPUT);
            result = new Mutex(matcher.group(MUTEX_NAME_GROUP), r1, g1, r2, g2);
        }
        return result;
    }

}
