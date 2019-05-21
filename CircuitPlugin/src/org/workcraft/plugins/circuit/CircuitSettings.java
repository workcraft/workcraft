package org.workcraft.plugins.circuit;

import org.workcraft.Config;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;
import org.workcraft.plugins.circuit.utils.Gate2;
import org.workcraft.plugins.circuit.utils.Gate3;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.DialogUtils;

import java.awt.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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

    private static final String keyBorderWidth = prefix + ".borderWidth";
    private static final String keyWireWidth = prefix + ".wireWidth";
    private static final String keyShowContacts = prefix + ".showContacts";
    private static final String keyShowZeroDelayNames = prefix + ".showZeroDelayNames";
    private static final String keyActiveWireColor = prefix + ".activeWireColor";
    private static final String keyInactiveWireColor = prefix + ".inactiveWireColor";
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
    private static final String keyResetPort = prefix + ".resetPort";
    private static final String keyResetPin = prefix + ".resetPin";
    private static final String keyTbufData = prefix + ".tbufData";
    private static final String keyScanPorts = prefix + ".scanPorts";
    private static final String keyScanPins = prefix + ".scanPins";
    private static final String keyScanSuffix = prefix + ".scanSuffix";
    private static final String keyVerilogAssignDelay = prefix + ".verilogAssignDelay";

    private static final Double defaultBorderWidth = 0.06;
    private static final Double defaultWireWidth = 0.04;
    private static final boolean defaultShowContacts = false;
    private static final boolean defaultShowZeroDelayNames = false;
    private static final Color defaultActiveWireColor = new Color(1.0f, 0.0f, 0.0f);
    private static final Color defaultInactiveWireColor = new Color(0.0f, 0.0f, 1.0f);
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
    private static final String defaultResetPort = "reset";
    private static final String defaultResetPin = "R";
    private static final String defaultTbufData = "TBUF (I, O)";
    private static final String defaultScanPorts = "scanin, clock";
    private static final String defaultScanPins = "SI, CK";
    private static final String defaultScanSuffix = "_scan";
    private static final boolean defaultVerilogAssignDelay = false;

    private static Double borderWidth = defaultBorderWidth;
    private static Double wireWidth = defaultWireWidth;
    private static boolean showContacts = defaultShowContacts;
    private static boolean showZeroDelayNames = defaultShowZeroDelayNames;
    private static Color activeWireColor = defaultActiveWireColor;
    private static Color inactiveWireColor = defaultInactiveWireColor;
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
    private static String resetPort = defaultResetPort;
    private static String resetPin = defaultResetPin;
    private static String tbufData = defaultTbufData;
    private static String scanPorts = defaultScanPorts;
    private static String scanPins = defaultScanPins;
    private static String scanSuffix = defaultScanSuffix;
    private static boolean verilogAssignDelay = defaultVerilogAssignDelay;

    public CircuitSettings() {
        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Show contacts", Boolean.class) {
            @Override
            public void setter(CircuitSettings object, Boolean value) {
                setShowContacts(value);
            }
            @Override
            public Boolean getter(CircuitSettings object) {
                return getShowContacts();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Show names of zero-dealy components", Boolean.class) {
            @Override
            public void setter(CircuitSettings object, Boolean value) {
                setShowZeroDelayNames(value);
            }
            @Override
            public Boolean getter(CircuitSettings object) {
                return getShowZeroDelayNames();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Double>(
                this, "Border width", Double.class) {
            @Override
            public void setter(CircuitSettings object, Double value) {
                setBorderWidth(value);
            }
            @Override
            public Double getter(CircuitSettings object) {
                return getBorderWidth();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Double>(
                this, "Wire width", Double.class) {
            @Override
            public void setter(CircuitSettings object, Double value) {
                setWireWidth(value);
            }
            @Override
            public Double getter(CircuitSettings object) {
                return getWireWidth();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Active wire", Color.class) {
            @Override
            public void setter(CircuitSettings object, Color value) {
                setActiveWireColor(value);
            }
            @Override
            public Color getter(CircuitSettings object) {
                return getActiveWireColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Color>(
                this, "Inactive wire", Color.class) {
            @Override
            public void setter(CircuitSettings object, Color value) {
                setInactiveWireColor(value);
            }
            @Override
            public Color getter(CircuitSettings object) {
                return getInactiveWireColor();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Simplify generated circuit STG", Boolean.class) {
            @Override
            public void setter(CircuitSettings object, Boolean value) {
                setSimplifyStg(value);
            }
            @Override
            public Boolean getter(CircuitSettings object) {
                return getSimplifyStg();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Gate library for technology mapping", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setGateLibrary(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getGateLibrary();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Substitution rules for export", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setSubstitutionLibrary(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getSubstitutionLibrary();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "BUF name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate2Data(value) != null) {
                    setBufData(value);
                } else {
                    DialogUtils.showError("BUF description format is incorrect. It should be as follows:\n" + defaultBufData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getBufData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "AND2 name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setAndData(value);
                } else {
                    DialogUtils.showError("AND2 description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getAndData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "OR2 name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setOrData(value);
                } else {
                    DialogUtils.showError("OR2 description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getOrData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NAND2 name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNandData(value);
                } else {
                    DialogUtils.showError("NAND2 description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getNandData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NOR2-gate name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNorData(value);
                } else {
                    DialogUtils.showError("NOR2 description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getNorData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NAND2B name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNandbData(value);
                } else {
                    DialogUtils.showError("NAND2B description format is incorrect. It should be as follows:\n" + defaultAndData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getNandbData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "NOR2B-gate name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate3Data(value) != null) {
                    setNorbData(value);
                } else {
                    DialogUtils.showError("NOR2B description format is incorrect. It should be as follows:\n" + defaultOrData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getNorbData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Mutex name and request-grant pairs", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseMutexData(value) != null) {
                    setMutexData(value);
                } else {
                    DialogUtils.showError("Mutex description format is incorrect. It should be as follows:\n" + defaultMutexData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getMutexData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Mutex.Protocol>(
                this, "Mutex protocol", Mutex.Protocol.class) {
            @Override
            public void setter(CircuitSettings object, Mutex.Protocol value) {
                StgSettings.setMutexProtocol(value);
            }
            @Override
            public Mutex.Protocol getter(CircuitSettings object) {
                return StgSettings.getMutexProtocol();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Bus split suffix ($ is replaced by index)", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setBusSuffix(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getBusSuffix();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Reset port name", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setResetPort(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getResetPort();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Reset pin name", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setResetPin(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getResetPin();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Testable buffer name and input-output pins", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (parseGate2Data(value) != null) {
                    setTbufData(value);
                } else {
                    DialogUtils.showError("Testable buffer description format is incorrect. It should be as follows:\n" + defaultTbufData);
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getTbufData();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Scan ports (comma-separated, same order as scan pins)", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (ReferenceHelper.parseReferenceList(value) != null) {
                    setScanPorts(value);
                } else {
                    DialogUtils.showError("Scan ports should be a comma-separated list of valid names.");
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getScanPorts();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Scan pins (comma-separated, same order as scan ports)", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                if (ReferenceHelper.parseReferenceList(value) != null) {
                    setScanPins(value);
                } else {
                    DialogUtils.showError("Scan pins should be a comma-separated list of valid names.");
                }
            }
            @Override
            public String getter(CircuitSettings object) {
                return getScanPins();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, String>(
                this, "Scan module suffix", String.class) {
            @Override
            public void setter(CircuitSettings object, String value) {
                setScanSuffix(value);
            }
            @Override
            public String getter(CircuitSettings object) {
                return getScanSuffix();
            }
        });

        properties.add(new PropertyDeclaration<CircuitSettings, Boolean>(
                this, "Delay assign statements in Verilog export", Boolean.class) {
            @Override
            public void setter(CircuitSettings object, Boolean value) {
                setVerilogAssignDelay(value);
            }
            @Override
            public Boolean getter(CircuitSettings object) {
                return getVerilogAssignDelay();
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
        setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
        setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
        setShowContacts(config.getBoolean(keyShowContacts, defaultShowContacts));
        setShowZeroDelayNames(config.getBoolean(keyShowZeroDelayNames, defaultShowZeroDelayNames));
        setActiveWireColor(config.getColor(keyActiveWireColor, defaultActiveWireColor));
        setInactiveWireColor(config.getColor(keyInactiveWireColor, defaultInactiveWireColor));
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
        setResetPort(config.getString(keyResetPort, defaultResetPort));
        setResetPin(config.getString(keyResetPin, defaultResetPin));
        setTbufData(config.getString(keyTbufData, defaultTbufData));
        setScanPorts(config.getString(keyScanPorts, defaultScanPorts));
        setScanPins(config.getString(keyScanPins, defaultScanPins));
        setScanSuffix(config.getString(keyScanSuffix, defaultScanSuffix));
        setVerilogAssignDelay(config.getBoolean(keyVerilogAssignDelay, defaultVerilogAssignDelay));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyBorderWidth, getBorderWidth());
        config.setDouble(keyWireWidth, getWireWidth());
        config.setBoolean(keyShowContacts, getShowContacts());
        config.setBoolean(keyShowZeroDelayNames, getShowZeroDelayNames());
        config.setColor(keyActiveWireColor, getActiveWireColor());
        config.setColor(keyInactiveWireColor, getInactiveWireColor());
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
        config.set(keyResetPort, getResetPort());
        config.set(keyResetPin, getResetPin());
        config.set(keyTbufData, getTbufData());
        config.set(keyScanPorts, getScanPorts());
        config.set(keyScanPins, getScanPins());
        config.set(keyScanSuffix, getScanSuffix());
        config.setBoolean(keyVerilogAssignDelay, getVerilogAssignDelay());
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

    public static String getResetPort() {
        return resetPort;
    }

    public static void setResetPort(String value) {
        resetPort = value;
    }

    public static String getResetPin() {
        return resetPin;
    }

    public static void setResetPin(String value) {
        resetPin = value;
    }

    public static String getTbufData() {
        return tbufData;
    }

    public static void setTbufData(String value) {
        tbufData = value;
    }

    public static Gate2 parseTbufData() {
        return parseGate2Data(getTbufData());
    }

    public static String getScanPorts() {
        return scanPorts;
    }

    public static void setScanPorts(String value) {
        scanPorts = value;
    }

    public static List<String> parseScanPorts() {
        return ReferenceHelper.parseReferenceList(getScanPorts());
    }

    public static String getScanPins() {
        return scanPins;
    }

    public static void setScanPins(String value) {
        scanPins = value;
    }

    public static List<String> parseScanPins() {
        return ReferenceHelper.parseReferenceList(getScanPins());
    }

    public static String getScanSuffix() {
        return scanSuffix;
    }

    public static void setScanSuffix(String value) {
        scanSuffix = value;
    }

    public static boolean getVerilogAssignDelay() {
        return verilogAssignDelay;
    }

    public static void setVerilogAssignDelay(boolean value) {
        verilogAssignDelay = value;
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
            String name = matcher.group(MUTEX_NAME_GROUP);
            Signal r1 = new Signal(matcher.group(MUTEX_R1_GROUP), Signal.Type.INPUT);
            Signal g1 = new Signal(matcher.group(MUTEX_G1_GROUP), Signal.Type.OUTPUT);
            Signal r2 = new Signal(matcher.group(MUTEX_R2_GROUP), Signal.Type.INPUT);
            Signal g2 = new Signal(matcher.group(MUTEX_G2_GROUP), Signal.Type.OUTPUT);
            result = new Mutex(name, r1, g1, r2, g2);
        }
        return result;
    }

}
