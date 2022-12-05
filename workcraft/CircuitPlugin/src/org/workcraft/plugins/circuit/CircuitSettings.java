package org.workcraft.plugins.circuit;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.plugins.circuit.genlib.GateInterface;
import org.workcraft.plugins.circuit.utils.InitMappingRules;
import org.workcraft.plugins.circuit.utils.VerilogUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.StgSettings;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.FileFilters;

import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CircuitSettings extends AbstractModelSettings {

    public static final String GATE_LIBRARY_TITLE = "Gate library for technology mapping";

    private static final Pattern MUTEX_DATA_PATTERN = Pattern.compile(
            "(\\w+)\\(\\((\\w+),(\\w+)\\),\\((\\w+),(\\w+)\\)\\)");

    private static final int MUTEX_NAME_GROUP = 1;
    private static final int MUTEX_R1_GROUP = 2;
    private static final int MUTEX_G1_GROUP = 3;
    private static final int MUTEX_R2_GROUP = 4;
    private static final int MUTEX_G2_GROUP = 5;

    private static final Pattern WAIT_DATA_PATTERN = Pattern.compile(
            "(\\w+)\\((\\w+),\\((\\w+),(\\w+)\\)\\)");

    private static final int WAIT_NAME_GROUP = 1;
    private static final int WAIT_SIG_GROUP = 2;
    private static final int WAIT_CTRL_GROUP = 3;
    private static final int WAIT_SAN_GROUP = 4;

    private static final Pattern GATE2_DATA_PATTERN = Pattern.compile("(\\w+)\\((\\w+),(\\w+)\\)");
    private static final int GATE_NAME_GROUP = 1;
    private static final int GATE_INPUT_GROUP = 2;
    private static final int GATE_OUTPUT_GROUP = 3;

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CircuitSettings";

    /*
     * Keys
     */
    private static final String keyContactFontSize = prefix + ".contactFontSize";
    private static final String keyShowZeroDelayNames = prefix + ".showZeroDelayNames";
    private static final String keyBorderWidth = prefix + ".borderWidth";
    private static final String keyWireWidth = prefix + ".wireWidth";
    private static final String keyActiveWireColor = prefix + ".activeWireColor";
    private static final String keyInactiveWireColor = prefix + ".inactiveWireColor";
    private static final String keySimplifyStg = prefix + ".simplifyStg";
    // Gate library
    private static final String keyGateLibrary = prefix + ".gateLibrary";
    private static final String keyWaitData = prefix + ".waitData";
    private static final String keyWait0Data = prefix + ".wait0Data";
    private static final String keyMutexData = prefix + ".mutexData";
    private static final String keyMutexLateSuffix = prefix + ".mutexLateSuffix";
    private static final String keyMutexEarlySuffix = prefix + ".mutexEarlySuffix";
    // Import/export
    private static final String keyExportSubstitutionLibrary = prefix + ".exportSubstitutionLibrary";
    private static final String keyInvertExportSubstitutionRules = prefix + ".invertExportSubstitutionRules";
    private static final String keyImportSubstitutionLibrary = prefix + ".importSubstitutionLibrary";
    private static final String keyInvertImportSubstitutionRules = prefix + ".invertImportSubstitutionRules";
    private static final String keyVerilogAssignDelay = prefix + ".verilogAssignDelay";
    private static final String keyBusSuffix = prefix + ".busSuffix";
    private static final String keyModuleFilePattern = prefix + ".moduleFilePattern";
    // Reset
    private static final String keyResetActiveHighPort = prefix + ".resetActiveHighPort";
    private static final String keyResetActiveLowPort = prefix + ".resetActiveLowPort";
    private static final String keyInitLowMappingRules = prefix + ".initLowMappingRules";
    private static final String keyInitHighMappingRules = prefix + ".initHighMappingRules";
    // Scan
    private static final String keyTbufData = prefix + ".tbufData";
    private static final String keyTinvData = prefix + ".tinvData";
    private static final String keyTestInstancePrefix = prefix + ".testInstancePrefix";
    private static final String keyScanSuffix = prefix + ".scanSuffix";
    private static final String keyScaninData = prefix + ".scaninData";
    private static final String keyScanoutData = prefix + ".scanoutData";
    private static final String keyScanenData = prefix + ".scanenData";
    private static final String keyScanckData = prefix + ".scanckData";
    private static final String keyScantmData = prefix + ".scantmData";
    private static final String keyUseIndividualScan = prefix + ".useIndividualScan";
    private static final String keyUseScanInitialisation = prefix + ".useScanInitialisation";
    private static final String keyInitialisationInverterInstancePrefix = prefix + ".initialisationInverterInstancePrefix";

    /*
     * Defaults
     */
    private static final double defaultContactFontSize = 0.4f;
    private static final boolean defaultShowZeroDelayNames = false;
    private static final Double defaultBorderWidth = 0.06;
    private static final Double defaultWireWidth = 0.04;
    private static final Color defaultActiveWireColor = new Color(1.0f, 0.0f, 0.0f);
    private static final Color defaultInactiveWireColor = new Color(0.0f, 0.0f, 1.0f);
    private static final boolean defaultSimplifyStg = true;
    // Gate library
    private static final String defaultGateLibrary = BackendUtils.getLibraryPath("workcraft.lib");
    private static final String defaultWaitData = "WAIT (sig, (ctrl, san))";
    private static final String defaultWait0Data = "WAIT0 (sig, (ctrl, san))";
    private static final String defaultMutexData = "MUTEX ((r1, g1), (r2, g2))";
    private static final String defaultMutexLateSuffix = "";
    private static final String defaultMutexEarlySuffix = "_early";
    // Import/export
    private static final String defaultExportSubstitutionLibrary = "";
    private static final boolean defaultInvertExportSubstitutionRules = false;
    private static final String defaultImportSubstitutionLibrary = "";
    private static final boolean defaultInvertImportSubstitutionRules = true;
    private static final String defaultVerilogAssignDelay = "";
    private static final String defaultBusSuffix = "__" + VerilogUtils.BUS_INDEX_PLACEHOLDER;
    private static final String defaultModuleFilePattern = VerilogUtils.MODULE_NAME_PLACEHOLDER + FileFilters.DOCUMENT_EXTENSION;
    // Reset
    private static final String defaultResetActiveHighPort = "rst";
    private static final String defaultResetActiveLowPort = "rst_n";
    private static final String defaultInitLowMappingRules = "C2->C2R(R), NC2->NC2R(R)";
    private static final String defaultInitHighMappingRules = "C2->C2S(S), NC2->NC2S(S)";
    // Scan
    private static final String defaultTbufData = "TBUF (I, O)";
    private static final String defaultTinvData = "TINV (I, ON)";
    private static final String defaultTestInstancePrefix = "test_";
    private static final String defaultScanSuffix = "_scan";
    private static final String defaultScaninData = "scanin / SI";
    private static final String defaultScanoutData = "scanout / SO";
    private static final String defaultScanenData = "scanen / SE";
    private static final String defaultScanckData = "scanck / CK";
    private static final String defaultScantmData = "scantm / TM";
    private static final boolean defaultUseIndividualScan = false;
    private static final boolean defaultUseScanInitialisation = false;
    private static final String defaultInitialisationInverterInstancePrefix = "test_inv_";
    /*
     * Variables
     */
    private static double contactFontSize = defaultContactFontSize;
    private static boolean showZeroDelayNames = defaultShowZeroDelayNames;
    private static Double borderWidth = defaultBorderWidth;
    private static Double wireWidth = defaultWireWidth;
    private static Color activeWireColor = defaultActiveWireColor;
    private static Color inactiveWireColor = defaultInactiveWireColor;
    private static boolean simplifyStg = defaultSimplifyStg;
    // Gate library
    private static String gateLibrary = defaultGateLibrary;
    private static String waitData = defaultWaitData;
    private static String wait0Data = defaultWait0Data;
    private static String mutexData = defaultMutexData;
    private static String mutexLateSuffix = defaultMutexLateSuffix;
    private static String mutexEarlySuffix = defaultMutexEarlySuffix;
    // Import/export
    private static String exportSubstitutionLibrary = defaultExportSubstitutionLibrary;
    private static boolean invertExportSubstitutionRules = defaultInvertExportSubstitutionRules;
    private static String importSubstitutionLibrary = defaultImportSubstitutionLibrary;
    private static boolean invertImportSubstitutionRules = defaultInvertImportSubstitutionRules;
    private static String verilogAssignDelay = defaultVerilogAssignDelay;
    private static String busSuffix = defaultBusSuffix;
    private static String moduleFilePattern = defaultModuleFilePattern;
    // Reset
    private static String resetActiveHighPort = defaultResetActiveHighPort;
    private static String resetActiveLowPort = defaultResetActiveLowPort;
    private static String initLowMappingRules = defaultInitLowMappingRules;
    private static String initHighMappingRules = defaultInitHighMappingRules;
    // Scan
    private static String tbufData = defaultTbufData;
    private static String tinvData = defaultTinvData;
    private static String testInstancePrefix = defaultTestInstancePrefix;
    private static String scanSuffix = defaultScanSuffix;
    private static String scaninData = defaultScaninData;
    private static String scanoutData = defaultScanoutData;
    private static String scanenData = defaultScanenData;
    private static String scanckData = defaultScanckData;
    private static String scantmData = defaultScantmData;
    private static boolean useIndividualScan = defaultUseIndividualScan;
    private static boolean useScanInitialisation = defaultUseScanInitialisation;
    private static String initialisationInverterInstancePrefix = defaultInitialisationInverterInstancePrefix;

    static {
        properties.add(new PropertyDeclaration<>(Double.class,
                "Contact font size (cm)",
                CircuitSettings::setContactFontSize,
                CircuitSettings::getContactFontSize));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Show names of zero-delay components",
                CircuitSettings::setShowZeroDelayNames,
                CircuitSettings::getShowZeroDelayNames));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Border width",
                CircuitSettings::setBorderWidth,
                CircuitSettings::getBorderWidth));

        properties.add(new PropertyDeclaration<>(Double.class,
                "Wire width",
                CircuitSettings::setWireWidth,
                CircuitSettings::getWireWidth));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Active wire",
                CircuitSettings::setActiveWireColor,
                CircuitSettings::getActiveWireColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                "Inactive wire",
                CircuitSettings::setInactiveWireColor,
                CircuitSettings::getInactiveWireColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Simplify generated circuit STG",
                CircuitSettings::setSimplifyStg,
                CircuitSettings::getSimplifyStg));

        properties.add(PropertyHelper.createSeparatorProperty("Gates and arbitration primitives"));

        properties.add(new PropertyDeclaration<>(File.class,
                GATE_LIBRARY_TITLE,
                value -> setGateLibrary(getBaseRelativePath(value)),
                () -> getBaseRelativeFile(getGateLibrary())));

        properties.add(new PropertyDeclaration<>(String.class,
                "WAIT name, dirty input and clean handshake",
                value -> {
                    if (parseWaitData(value) != null) {
                        setWaitData(value);
                    } else {
                        errorDescriptionFormat("WAIT", defaultWaitData);
                    }
                },
                CircuitSettings::getWaitData));

        properties.add(new PropertyDeclaration<>(String.class,
                "WAIT0 name, dirty input and clean handshake",
                value -> {
                    if (parseWaitData(value) != null) {
                        setWait0Data(value);
                    } else {
                        errorDescriptionFormat("WAIT0", defaultWait0Data);
                    }
                },
                CircuitSettings::getWait0Data));

        properties.add(new PropertyDeclaration<>(String.class,
                "MUTEX name and request-grant pairs",
                value -> {
                    if (parseMutexDataOrNull(value) != null) {
                        setMutexData(value);
                    } else {
                        errorDescriptionFormat("MUTEX", defaultMutexData);
                    }
                },
                CircuitSettings::getMutexData));

        properties.add(new PropertyDeclaration<>(String.class,
                "Late protocol MUTEX module suffix",
                CircuitSettings::setMutexLateSuffix,
                CircuitSettings::getMutexLateSuffix));

        properties.add(new PropertyDeclaration<>(String.class,
                "Early protocol MUTEX module suffix",
                CircuitSettings::setMutexEarlySuffix,
                CircuitSettings::getMutexEarlySuffix));

        properties.add(new PropertyDeclaration<>(Mutex.Protocol.class,
                "MUTEX protocol",
                StgSettings::setMutexProtocol,
                StgSettings::getMutexProtocol));

        properties.add(PropertyHelper.createSeparatorProperty("Verilog import and export"));

        properties.add(new PropertyDeclaration<>(File.class,
                PropertyHelper.BULLET_PREFIX + "Substitution rules for export",
                value -> setExportSubstitutionLibrary(getBaseRelativePath(value)),
                () -> getBaseRelativeFile(getExportSubstitutionLibrary())));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Invert substitution rules for export",
                CircuitSettings::setInvertExportSubstitutionRules,
                CircuitSettings::getInvertExportSubstitutionRules));

        properties.add(new PropertyDeclaration<>(File.class,
                PropertyHelper.BULLET_PREFIX + "Substitution rules for import",
                value -> setImportSubstitutionLibrary(getBaseRelativePath(value)),
                () -> getBaseRelativeFile(getImportSubstitutionLibrary())));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Invert substitution rules for import",
                CircuitSettings::setInvertImportSubstitutionRules,
                CircuitSettings::getInvertImportSubstitutionRules));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Delay for assign statements in Verilog export (empty to suppress)",
                CircuitSettings::setVerilogAssignDelay,
                CircuitSettings::getVerilogAssignDelay));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Bus split/merge suffix on Verilog import/export ("
                        + VerilogUtils.BUS_INDEX_PLACEHOLDER + " denotes index)",
                CircuitSettings::setBusSuffix,
                CircuitSettings::getBusSuffix));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "File pattern for import of hierarchical Verilog modules ("
                        + VerilogUtils.MODULE_NAME_PLACEHOLDER + " denotes module name)",
                CircuitSettings::setModuleFilePattern,
                CircuitSettings::getModuleFilePattern));

        properties.add(PropertyHelper.createSeparatorProperty("Initialisation"));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Active-high reset port name",
                CircuitSettings::setResetActiveHighPort,
                CircuitSettings::getResetActiveHighPort));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Active-low reset port name",
                CircuitSettings::setResetActiveLowPort,
                CircuitSettings::getResetActiveLowPort));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Rules for init-low gates as comma-separated list of original_gate->replacement_gate(init_pin)",
                CircuitSettings::setInitLowMappingRules,
                CircuitSettings::getInitLowMappingRules));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Rules for init-high gates as comma-separated list of original_gate->replacement_gate(init_pin)",
                CircuitSettings::setInitHighMappingRules,
                CircuitSettings::getInitHighMappingRules));

        properties.add(PropertyHelper.createSeparatorProperty("Loop breaking and scan insertion"));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Testable buffer name and input-output pins",
                value -> setGate2Data(value, CircuitSettings::setTbufData, "Testable buffer", defaultTbufData),
                CircuitSettings::getTbufData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Testable inverter name and input-output pins",
                value -> setGate2Data(value, CircuitSettings::setTinvData, "Testable inverter", defaultTinvData),
                CircuitSettings::getTinvData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Testable instance prefix",
                CircuitSettings::setTestInstancePrefix,
                CircuitSettings::getTestInstancePrefix));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan module suffix",
                CircuitSettings::setScanSuffix,
                CircuitSettings::getScanSuffix));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan input port/pin (empty to skip)",
                value -> setPortPinPair(value, CircuitSettings::setScaninData, "Scan input"),
                                CircuitSettings::getScaninData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan output port/pin (empty to skip)",
                value -> setPortPinPair(value, CircuitSettings::setScanoutData, "Scan output"),
                CircuitSettings::getScanoutData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan enable port/pin (empty to skip)",
                value -> setPortPinPair(value, CircuitSettings::setScanenData, "Scan enable"),
                CircuitSettings::getScanenData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan clock port/pin (empty to skip)",
                value -> setPortPinPair(value, CircuitSettings::setScanckData, "Scan clock"),
                CircuitSettings::getScanckData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Scan test mode port/pin (empty to skip)",
                value -> setPortPinPair(value, CircuitSettings::setScantmData, "Scan test mode"),
                CircuitSettings::getScantmData));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Individual scan control and observation",
                CircuitSettings::setUseIndividualScan,
                CircuitSettings::getUseIndividualScan));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Use Scan for initialisation",
                CircuitSettings::setUseScanInitialisation,
                CircuitSettings::getUseScanInitialisation));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Initialisation inverter instance prefix",
                CircuitSettings::setInitialisationInverterInstancePrefix,
                CircuitSettings::getInitialisationInverterInstancePrefix));
    }

    private static String getBaseRelativePath(File file) {
        return file == null ? "" : FileUtils.stripBase(file.getPath(), System.getProperty("user.dir"));
    }

    private static File getBaseRelativeFile(String path) {
        return (path == null) || path.isEmpty() ? null : new File(path);
    }

    private static void errorDescriptionFormat(String prefix, String suffix) {
        DialogUtils.showError(prefix + " description format is incorrect. It should be as follows:\n" + suffix);
    }

    private static void errorPortPinFormat(String prefix) {
        DialogUtils.showError(prefix + " port and pin should be a pair of valid names separated by '/'.");
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getName() {
        return "Digital Circuit";
    }

    @Override
    public void load(Config config) {
        setContactFontSize(config.getDouble(keyContactFontSize, defaultContactFontSize));
        setShowZeroDelayNames(config.getBoolean(keyShowZeroDelayNames, defaultShowZeroDelayNames));
        setBorderWidth(config.getDouble(keyBorderWidth, defaultBorderWidth));
        setWireWidth(config.getDouble(keyWireWidth, defaultWireWidth));
        setActiveWireColor(config.getColor(keyActiveWireColor, defaultActiveWireColor));
        setInactiveWireColor(config.getColor(keyInactiveWireColor, defaultInactiveWireColor));
        setSimplifyStg(config.getBoolean(keySimplifyStg, defaultSimplifyStg));
        // Gate library
        setGateLibrary(config.getString(keyGateLibrary, defaultGateLibrary));
        setWaitData(config.getString(keyWaitData, defaultWaitData));
        setWait0Data(config.getString(keyWait0Data, defaultWait0Data));
        setMutexData(config.getString(keyMutexData, defaultMutexData));
        setMutexLateSuffix(config.getString(keyMutexLateSuffix, defaultMutexLateSuffix));
        setMutexEarlySuffix(config.getString(keyMutexEarlySuffix, defaultMutexEarlySuffix));
        // Import/export
        setExportSubstitutionLibrary(config.getString(keyExportSubstitutionLibrary, defaultExportSubstitutionLibrary));
        setInvertExportSubstitutionRules(config.getBoolean(keyInvertExportSubstitutionRules, defaultInvertExportSubstitutionRules));
        setImportSubstitutionLibrary(config.getString(keyImportSubstitutionLibrary, defaultImportSubstitutionLibrary));
        setInvertImportSubstitutionRules(config.getBoolean(keyInvertImportSubstitutionRules, defaultInvertImportSubstitutionRules));
        setVerilogAssignDelay(config.getString(keyVerilogAssignDelay, defaultVerilogAssignDelay));
        setBusSuffix(config.getString(keyBusSuffix, defaultBusSuffix));
        setModuleFilePattern(config.getString(keyModuleFilePattern, defaultModuleFilePattern));
        // Reset
        setResetActiveHighPort(config.getString(keyResetActiveHighPort, defaultResetActiveHighPort));
        setResetActiveLowPort(config.getString(keyResetActiveLowPort, defaultResetActiveLowPort));
        setInitLowMappingRules(config.getString(keyInitLowMappingRules, defaultInitLowMappingRules));
        setInitHighMappingRules(config.getString(keyInitHighMappingRules, defaultInitHighMappingRules));
        // Scan
        setTbufData(config.getString(keyTbufData, defaultTbufData));
        setTinvData(config.getString(keyTinvData, defaultTinvData));
        setTestInstancePrefix(config.getString(keyTestInstancePrefix, defaultTestInstancePrefix));
        setScanSuffix(config.getString(keyScanSuffix, defaultScanSuffix));
        setScaninData(config.getString(keyScaninData, defaultScaninData));
        setScanoutData(config.getString(keyScanoutData, defaultScanoutData));
        setScanenData(config.getString(keyScanenData, defaultScanenData));
        setScanckData(config.getString(keyScanckData, defaultScanckData));
        setScantmData(config.getString(keyScantmData, defaultScantmData));
        setUseIndividualScan(config.getBoolean(keyUseIndividualScan, defaultUseIndividualScan));
        setUseScanInitialisation(config.getBoolean(keyUseScanInitialisation, defaultUseScanInitialisation));
        setInitialisationInverterInstancePrefix(config.getString(keyInitialisationInverterInstancePrefix, defaultInitialisationInverterInstancePrefix));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyContactFontSize, getContactFontSize());
        config.setBoolean(keyShowZeroDelayNames, getShowZeroDelayNames());
        config.setDouble(keyBorderWidth, getBorderWidth());
        config.setDouble(keyWireWidth, getWireWidth());
        config.setColor(keyActiveWireColor, getActiveWireColor());
        config.setColor(keyInactiveWireColor, getInactiveWireColor());
        config.setBoolean(keySimplifyStg, getSimplifyStg());
        // Gate library
        config.set(keyGateLibrary, getGateLibrary());
        config.set(keyWaitData, getWaitData());
        config.set(keyWait0Data, getWait0Data());
        config.set(keyMutexData, getMutexData());
        config.set(keyMutexLateSuffix, getMutexLateSuffix());
        config.set(keyMutexEarlySuffix, getMutexEarlySuffix());
        // Import/export
        config.set(keyExportSubstitutionLibrary, getExportSubstitutionLibrary());
        config.setBoolean(keyInvertExportSubstitutionRules, getInvertExportSubstitutionRules());
        config.set(keyImportSubstitutionLibrary, getImportSubstitutionLibrary());
        config.setBoolean(keyInvertImportSubstitutionRules, getInvertImportSubstitutionRules());
        config.set(keyVerilogAssignDelay, getVerilogAssignDelay());
        config.set(keyBusSuffix, getBusSuffix());
        config.set(keyModuleFilePattern, getModuleFilePattern());
        // Reset
        config.set(keyResetActiveHighPort, getResetActiveHighPort());
        config.set(keyResetActiveLowPort, getResetActiveLowPort());
        config.set(keyInitLowMappingRules, getInitLowMappingRules());
        config.set(keyInitHighMappingRules, getInitHighMappingRules());
        // Scan
        config.set(keyTbufData, getTbufData());
        config.set(keyTinvData, getTinvData());
        config.set(keyTestInstancePrefix, getTestInstancePrefix());
        config.set(keyScanSuffix, getScanSuffix());
        config.set(keyScaninData, getScaninData());
        config.set(keyScanoutData, getScanoutData());
        config.set(keyScanenData, getScanenData());
        config.set(keyScanckData, getScanckData());
        config.set(keyScantmData, getScantmData());
        config.setBoolean(keyUseIndividualScan, getUseIndividualScan());
        config.setBoolean(keyUseScanInitialisation, getUseScanInitialisation());
        config.set(keyInitialisationInverterInstancePrefix, getInitialisationInverterInstancePrefix());
    }

    public static double getContactFontSize() {
        return contactFontSize;
    }

    public static void setContactFontSize(double value) {
        contactFontSize = value;
    }

    public static boolean getShowZeroDelayNames() {
        return showZeroDelayNames;
    }

    public static void setShowZeroDelayNames(boolean value) {
        showZeroDelayNames = value;
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

    public static String getWaitData() {
        return waitData;
    }

    public static void setWaitData(String value) {
        waitData = value;
    }

    public static String getWait0Data() {
        return wait0Data;
    }

    public static void setWait0Data(String value) {
        wait0Data = value;
    }

    public static Wait parseWaitData(Wait.Type type) {
        return parseWaitData(type == Wait.Type.WAIT0 ? getWait0Data() : getWaitData());
    }

    public static String getMutexData() {
        return mutexData;
    }

    public static void setMutexData(String value) {
        mutexData = value;
    }

    public static Mutex parseMutexData() {
        return parseMutexDataOrNull(getMutexData());
    }

    public static String getMutexLateSuffix() {
        return mutexLateSuffix;
    }

    public static void setMutexLateSuffix(String value) {
        mutexLateSuffix = value;
    }

    public static String getMutexEarlySuffix() {
        return mutexEarlySuffix;
    }

    public static void setMutexEarlySuffix(String value) {
        mutexEarlySuffix = value;
    }

    public static String getExportSubstitutionLibrary() {
        return exportSubstitutionLibrary;
    }

    public static void setExportSubstitutionLibrary(String value) {
        exportSubstitutionLibrary = value;
    }

    public static boolean getInvertExportSubstitutionRules() {
        return invertExportSubstitutionRules;
    }

    public static void setInvertExportSubstitutionRules(boolean value) {
        invertExportSubstitutionRules = value;
    }

    public static String getImportSubstitutionLibrary() {
        return importSubstitutionLibrary;
    }

    public static void setImportSubstitutionLibrary(String value) {
        importSubstitutionLibrary = value;
    }

    public static boolean getInvertImportSubstitutionRules() {
        return invertImportSubstitutionRules;
    }

    public static void setInvertImportSubstitutionRules(boolean value) {
        invertImportSubstitutionRules = value;
    }

    public static String getVerilogAssignDelay() {
        return verilogAssignDelay;
    }

    public static void setVerilogAssignDelay(String value) {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (VerilogUtils.checkAssignDelay(value)) {
            verilogAssignDelay = value;
        } else {
            DialogUtils.showError(VerilogUtils.getAssignDelayHelp());
        }
    }

    public static String getBusSuffix() {
        return busSuffix;
    }

    public static void setBusSuffix(String value) {
        if ((value == null) || !value.contains(VerilogUtils.BUS_INDEX_PLACEHOLDER)) {
            DialogUtils.showError("Bus suffix must have index placeholder " + VerilogUtils.BUS_INDEX_PLACEHOLDER);
        } else {
            busSuffix = value;
        }
    }

    public static String getModuleFilePattern() {
        return moduleFilePattern;
    }

    public static void setModuleFilePattern(String value) {
        if ((value == null) || !value.contains(VerilogUtils.MODULE_NAME_PLACEHOLDER)) {
            DialogUtils.showError("File pattern must have module name placeholder " + VerilogUtils.MODULE_NAME_PLACEHOLDER);
        } else {
            moduleFilePattern = value;
        }
    }

    public static String getResetActiveHighPort() {
        return resetActiveHighPort;
    }

    public static void setResetActiveHighPort(String value) {
        resetActiveHighPort = value;
    }

    public static String getResetActiveLowPort() {
        return resetActiveLowPort;
    }

    public static void setResetActiveLowPort(String value) {
        resetActiveLowPort = value;
    }

    public static String getInitLowMappingRules() {
        return initLowMappingRules;
    }

    public static void setInitLowMappingRules(String value) {
        initLowMappingRules = InitMappingRules.sanitise(value);
    }

    public static Pair<String, String> getInitLowGatePinPair(String moduleName) {
        return InitMappingRules.parse(getInitLowMappingRules()).get(moduleName);
    }
    public static String getInitHighMappingRules() {
        return initHighMappingRules;
    }

    public static void setInitHighMappingRules(String value) {
        initHighMappingRules = InitMappingRules.sanitise(value);
    }

    public static Pair<String, String> getInitHighGatePinPair(String moduleName) {
        return InitMappingRules.parse(getInitHighMappingRules()).get(moduleName);
    }

    public static String getTbufData() {
        return tbufData;
    }

    public static void setTbufData(String value) {
        tbufData = value;
    }

    public static GateInterface parseTbufData() {
        return parseGate2DataOrNull(getTbufData());
    }

    public static String getTinvData() {
        return tinvData;
    }

    public static void setTinvData(String value) {
        tinvData = value;
    }

    public static GateInterface parseTinvData() {
        return parseGate2DataOrNull(getTinvData());
    }

    public static String getTestInstancePrefix() {
        return testInstancePrefix;
    }

    public static void setTestInstancePrefix(String value) {
        testInstancePrefix = value;
    }

    public static String getScanSuffix() {
        return scanSuffix;
    }

    public static void setScanSuffix(String value) {
        scanSuffix = value;
    }

    public static String getScaninData() {
        return scaninData;
    }

    public static void setScaninData(String value) {
        scaninData = value;
    }

    public static String getScaninPort() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScaninData());
        return pair == null ? null : pair.getFirst();
    }

    public static String getScaninPin() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScaninData());
        return pair == null ? null : pair.getSecond();
    }

    public static String getScanoutData() {
        return scanoutData;
    }

    public static void setScanoutData(String value) {
        scanoutData = value;
    }

    public static String getScanoutPort() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanoutData());
        return pair == null ? null : pair.getFirst();
    }

    public static String getScanoutPin() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanoutData());
        return pair == null ? null : pair.getSecond();
    }

    public static String getScanenData() {
        return scanenData;
    }

    public static void setScanenData(String value) {
        scanenData = value;
    }

    public static String getScanenPort() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanenData());
        return pair == null ? null : pair.getFirst();
    }

    public static String getScanenPin() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanenData());
        return pair == null ? null : pair.getSecond();
    }

    public static String getScanckData() {
        return scanckData;
    }

    public static void setScanckData(String value) {
        scanckData = value;
    }

    public static String getScanckPort() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanckData());
        return pair == null ? null : pair.getFirst();
    }

    public static String getScanckPin() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScanckData());
        return pair == null ? null : pair.getSecond();
    }

    public static String getScantmData() {
        return scantmData;
    }

    public static void setScantmData(String value) {
        scantmData = value;
    }

    public static String getScantmPort() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScantmData());
        return pair == null ? null : pair.getFirst();
    }

    public static String getScantmPin() {
        Pair<String, String> pair = parsePortPinPairOrNull(getScantmData());
        return pair == null ? null : pair.getSecond();
    }

    public static boolean getUseIndividualScan() {
        return useIndividualScan;
    }

    public static void setUseIndividualScan(boolean value) {
        useIndividualScan = value;
    }

    public static boolean getUseScanInitialisation() {
        return useScanInitialisation;
    }

    public static void setUseScanInitialisation(boolean value) {
        useScanInitialisation = value;
    }

    public static String getInitialisationInverterInstancePrefix() {
        return initialisationInverterInstancePrefix;
    }

    public static void setInitialisationInverterInstancePrefix(String value) {
        initialisationInverterInstancePrefix = value;
    }

    private static void setGate2Data(String value, Consumer<String> setter, String msg, String defaultValue) {
        if (parseGate2DataOrNull(value) != null) {
            setter.accept(value);
        } else {
            errorDescriptionFormat(msg, defaultValue);
        }
    }

    private static GateInterface parseGate2DataOrNull(String value) {
        Matcher matcher = GATE2_DATA_PATTERN.matcher(value.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(GATE_NAME_GROUP);
            String input = matcher.group(GATE_INPUT_GROUP);
            String output = matcher.group(GATE_OUTPUT_GROUP);
            return new GateInterface(name, Collections.singletonList(input), output);
        }
        return null;
    }

    private static Mutex parseMutexDataOrNull(String str) {
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

    private static Wait parseWaitData(String str) {
        Wait result = null;
        Matcher matcher = WAIT_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(WAIT_NAME_GROUP);
            Signal sig = new Signal(matcher.group(WAIT_SIG_GROUP), Signal.Type.INPUT);
            Signal ctrl = new Signal(matcher.group(WAIT_CTRL_GROUP), Signal.Type.INPUT);
            Signal san = new Signal(matcher.group(WAIT_SAN_GROUP), Signal.Type.OUTPUT);
            result = new Wait(name, sig, ctrl, san);
        }
        return result;
    }

    private static void setPortPinPair(String value, Consumer<String> setter, String msg) {
        if (parsePortPinPairOrNull(value) != null) {
            setter.accept(value);
        } else {
            errorPortPinFormat(msg);
        }
    }

    private static Pair<String, String> parsePortPinPairOrNull(String value) {
        if (value == null) {
            return null;
        }
        String valueWithoutSpaces = value.replaceAll("\\s", "");
        if (valueWithoutSpaces.isEmpty()) {
            return Pair.of(null, null);
        }
        String[] split = valueWithoutSpaces.split("/");
        String portName = (split.length > 0) ? split[0] : null;
        String pinName = (split.length > 1) ? split[1] : portName;
        return Pair.of(portName, pinName);
    }

}
