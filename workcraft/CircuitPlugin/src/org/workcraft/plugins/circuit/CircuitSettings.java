package org.workcraft.plugins.circuit;

import org.workcraft.Config;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.references.Identifier;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyHelper;
import org.workcraft.plugins.builtin.settings.AbstractModelSettings;
import org.workcraft.plugins.circuit.genlib.GateInterface;
import org.workcraft.plugins.circuit.utils.InitMappingRules;
import org.workcraft.plugins.circuit.utils.VerilogUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Wait;
import org.workcraft.types.Pair;
import org.workcraft.utils.BackendUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.FileFilters;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CircuitSettings extends AbstractModelSettings {

    public static final String DEFAULT_RANDOM_DELAY = "(1ps * $urandom_range(0, 50))";
    public static final String DEFAULT_RANDOM_DELAY_INTERVAL = "(1ps * $urandom_range(20, 50))";
    public static final Map<String, String> PREDEFINED_DELAY_PARAMETERS = new LinkedHashMap<>();

    static {
        PREDEFINED_DELAY_PARAMETERS.put("", "No delay");
        PREDEFINED_DELAY_PARAMETERS.put("1", "One unit delay (time unit and precision should be defined)");
        PREDEFINED_DELAY_PARAMETERS.put(DEFAULT_RANDOM_DELAY, "Random delay up to 50ps");
        PREDEFINED_DELAY_PARAMETERS.put(DEFAULT_RANDOM_DELAY_INTERVAL, "Random delay between 20ps and 50ps");
    }

    public static final Map<String, String> PREDEFINED_TIMESCALE_PARAMETERS = new LinkedHashMap<>();

    static {
        PREDEFINED_TIMESCALE_PARAMETERS.put("", "No delay");
        PREDEFINED_TIMESCALE_PARAMETERS.put("1ns / 1ps", "1ns per unit with 1ps precision");
    }

    public enum WaitUndefinedInterpretation {
        RANDOM("Random value"),
        HIGH("High value"),
        LOW("Low value");

        public final String name;

        WaitUndefinedInterpretation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public enum MutexArbitrationWinner {
        RANDOM("Random grant"),
        FIRST("First grant"),
        SECOND("Second grant");

        public final String name;

        MutexArbitrationWinner(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public static final String GATE_LIBRARY_TITLE = "Gate library for technology mapping";

    public enum ScanoutBufferingStyle {
        BUFFER_NEEDED("Buffer if needed"),
        INVERTER_NEEDED("Inverter if needed"),
        BUFFER_ALWAYS("Buffer always"),
        INVERTER_ALWAYS("Inverter always");

        public final String name;

        ScanoutBufferingStyle(String name) {
            this.name = name;
        }

        public boolean isInverted() {
            return (this == INVERTER_NEEDED) || (this == INVERTER_ALWAYS);
        }

        public boolean isAlways() {
            return (this == BUFFER_ALWAYS) || (this == INVERTER_ALWAYS);
        }

        @Override
        public String toString() {
            return name;
        }
    }

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

    private static final String BUS_INDEX_PLACEHOLDER = "$";
    private static final String MODULE_NAME_PLACEHOLDER = "$";
    private static final String FORK_FUNOUT_PLACEHOLDER = "$";
    private static final String INITIAL_STATE_PLACEHOLDER = "$";

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CircuitSettings";

    /*
     * Keys
     */
    private static final String keyContactFontSize = prefix + ".contactFontSize";
    private static final String keyShowZeroDelayNames = prefix + ".showZeroDelayNames";
    private static final String keyShowContactFunctions = prefix + ".showContactFunctions";
    private static final String keyShowContactFanout = prefix + ".showContactFanout";
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
    private static final String keyMutexLateData = prefix + ".mutexLateData";
    // Import/export
    private static final String keyExportSubstitutionLibrary = prefix + ".exportSubstitutionLibrary";
    private static final String keyInvertExportSubstitutionRules = prefix + ".invertExportSubstitutionRules";
    private static final String keyImportSubstitutionLibrary = prefix + ".importSubstitutionLibrary";
    private static final String keyInvertImportSubstitutionRules = prefix + ".invertImportSubstitutionRules";
    private static final String keyVerilogTimescale = prefix + ".verilogTimescale";
    private static final String keyVerilogAssignDelay = prefix + ".verilogAssignDelay";
    private static final String keyWaitSigIgnoreTime = prefix + ".waitSigIgnoreTime";
    private static final String keyWaitUndefinedInterpretation = prefix + ".waitUndefinedInterpretation";
    private static final String keyMutexEarlyGrantDelay = prefix + ".mutexGrantDelay";
    private static final String keyMutexArbitrationWinner = prefix + ".mutexArbitrationWinner";
    private static final String keyBusSuffix = prefix + ".busSuffix";
    private static final String keyDissolveSingletonBus = prefix + ".dissolveSingletonBus";
    private static final String keyAcceptInoutPort = prefix + ".acceptInoutPort";
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
    private static final String keyUseTestPathBreaker = prefix + ".useTestPathBreaker";
    private static final String keyScanSuffix = prefix + ".scanSuffix";
    private static final String keyScaninData = prefix + ".scaninData";
    private static final String keyScanoutData = prefix + ".scanoutData";
    private static final String keyScanenData = prefix + ".scanenData";
    private static final String keyScanckData = prefix + ".scanckData";
    private static final String keyScantmData = prefix + ".scantmData";
    private static final String keyUseIndividualScan = prefix + ".useIndividualScan";
    private static final String keyUseScanInitialisation = prefix + ".useScanInitialisation";
    private static final String keyInitialisationInverterInstancePrefix = prefix + ".initialisationInverterInstancePrefix";
    private static final String keyAuxiliaryPortRegex = prefix + ".auxiliaryPortRegex";
    // Forks
    private static final String keyForkHighFanout = prefix + ".forkHighFanout";
    private static final String keyForkBufferPattern = prefix + ".forkBufferPattern";
    private static final String keyScanoutBufferingStyle = prefix + ".scanoutBufferingStyle";

    /*
     * Defaults
     */
    private static final double defaultContactFontSize = 0.4f;
    private static final boolean defaultShowZeroDelayNames = false;
    private static final boolean defaultShowContactFunctions = true;
    private static final boolean defaultShowContactFanout = true;
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
    private static final String defaultMutexLateData = "MUTEX_late ((r1, g1), (r2, g2))";
    // Import/export
    private static final String defaultExportSubstitutionLibrary = "";
    private static final boolean defaultInvertExportSubstitutionRules = false;
    private static final String defaultImportSubstitutionLibrary = "";
    private static final boolean defaultInvertImportSubstitutionRules = true;
    private static final String defaultVerilogTimescale = "";
    private static final String defaultVerilogAssignDelay = "1";
    private static final String defaultWaitSigIgnoreTime = DEFAULT_RANDOM_DELAY_INTERVAL;
    private static final WaitUndefinedInterpretation defaultWaitUndefinedInterpretation = WaitUndefinedInterpretation.RANDOM;
    private static final String defaultMutexEarlyGrantDelay = DEFAULT_RANDOM_DELAY;
    private static final MutexArbitrationWinner defaultMutexArbitrationWinner = MutexArbitrationWinner.RANDOM;
    private static final String defaultBusSuffix = "__" + BUS_INDEX_PLACEHOLDER;
    private static final boolean defaultDissolveSingletonBus = true;
    private static final boolean defaultAcceptInoutPort = true;
    private static final String defaultModuleFilePattern = MODULE_NAME_PLACEHOLDER + FileFilters.DOCUMENT_EXTENSION;
    // Reset
    private static final String defaultResetActiveHighPort = "rst";
    private static final String defaultResetActiveLowPort = "rst_n";
    private static final String defaultInitLowMappingRules = "C2->C2R(R), NC2->NC2R(R)";
    private static final String defaultInitHighMappingRules = "C2->C2S(S), NC2->NC2S(S)";
    // Scan
    private static final String defaultTbufData = "TBUF (I, O)";
    private static final String defaultTinvData = "TINV (I, ON)";
    private static final String defaultTestInstancePrefix = "test_init" + INITIAL_STATE_PLACEHOLDER + "_";
    private static final boolean defaultUseTestPathBreaker = true;
    private static final String defaultScanSuffix = "_scan";
    private static final String defaultScaninData = "scanin / SI";
    private static final String defaultScanoutData = "scanout / SO";
    private static final String defaultScanenData = "scanen / SE";
    private static final String defaultScanckData = "scanck / CK";
    private static final String defaultScantmData = "scantm / TM";
    private static final boolean defaultUseIndividualScan = false;
    private static final boolean defaultUseScanInitialisation = false;
    private static final String defaultInitialisationInverterInstancePrefix = "test_inv_";
    private static final String defaultAuxiliaryPortRegex = "";
    // Forks
    private static final int defaultForkHighFanout = 4;
    private static final String defaultForkBufferPattern = "fork_x" + FORK_FUNOUT_PLACEHOLDER + "_";
    private static final ScanoutBufferingStyle defaultScanoutBufferingStyle = ScanoutBufferingStyle.BUFFER_NEEDED;

    /*
     * Variables
     */
    private static double contactFontSize = defaultContactFontSize;
    private static boolean showZeroDelayNames = defaultShowZeroDelayNames;
    private static boolean showContactFunctions = defaultShowContactFunctions;
    private static boolean showContactFanout = defaultShowContactFanout;
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
    private static String mutexLateData = defaultMutexLateData;
    // Import/export
    private static String exportSubstitutionLibrary = defaultExportSubstitutionLibrary;
    private static boolean invertExportSubstitutionRules = defaultInvertExportSubstitutionRules;
    private static String importSubstitutionLibrary = defaultImportSubstitutionLibrary;
    private static boolean invertImportSubstitutionRules = defaultInvertImportSubstitutionRules;
    private static String verilogTimescale = defaultVerilogTimescale;
    private static String verilogAssignDelay = defaultVerilogAssignDelay;
    private static String waitSigIgnoreTime = defaultWaitSigIgnoreTime;
    private static WaitUndefinedInterpretation waitUndefinedInterpretation = defaultWaitUndefinedInterpretation;
    private static String mutexEarlyGrantDelay = defaultMutexEarlyGrantDelay;
    private static MutexArbitrationWinner mutexArbitrationWinner = defaultMutexArbitrationWinner;
    private static String busSuffix = defaultBusSuffix;
    private static boolean dissolveSingletonBus = defaultDissolveSingletonBus;
    private static boolean acceptInoutPort = defaultAcceptInoutPort;
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
    private static boolean useTestPathBreaker = defaultUseTestPathBreaker;
    private static String scanSuffix = defaultScanSuffix;
    private static String scaninData = defaultScaninData;
    private static String scanoutData = defaultScanoutData;
    private static String scanenData = defaultScanenData;
    private static String scanckData = defaultScanckData;
    private static String scantmData = defaultScantmData;
    private static boolean useIndividualScan = defaultUseIndividualScan;
    private static boolean useScanInitialisation = defaultUseScanInitialisation;
    private static String initialisationInverterInstancePrefix = defaultInitialisationInverterInstancePrefix;
    private static String auxiliaryPortRegex = defaultAuxiliaryPortRegex;
    // Forks
    private static Integer forkHighFanout = defaultForkHighFanout;
    private static String forkBufferPattern = defaultForkBufferPattern;
    private static ScanoutBufferingStyle scanoutBufferingStyle = defaultScanoutBufferingStyle;

    static {
        properties.add(PropertyHelper.createSeparatorProperty("Visualisation"));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.BULLET_PREFIX + "Contact font size (cm)",
                CircuitSettings::setContactFontSize,
                CircuitSettings::getContactFontSize));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Show names of zero delay components",
                CircuitSettings::setShowZeroDelayNames,
                CircuitSettings::getShowZeroDelayNames));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Show set/reset functions of driver contacts",
                CircuitSettings::setShowContactFunctions,
                CircuitSettings::getShowContactFunctions));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Show fanout of driver contacts",
                CircuitSettings::setShowContactFanout,
                CircuitSettings::getShowContactFanout));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.BULLET_PREFIX + "Border width",
                CircuitSettings::setBorderWidth,
                CircuitSettings::getBorderWidth));

        properties.add(new PropertyDeclaration<>(Double.class,
                PropertyHelper.BULLET_PREFIX + "Wire width",
                CircuitSettings::setWireWidth,
                CircuitSettings::getWireWidth));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.BULLET_PREFIX + "Active wire",
                CircuitSettings::setActiveWireColor,
                CircuitSettings::getActiveWireColor));

        properties.add(new PropertyDeclaration<>(Color.class,
                PropertyHelper.BULLET_PREFIX + "Inactive wire",
                CircuitSettings::setInactiveWireColor,
                CircuitSettings::getInactiveWireColor));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Simplify generated circuit STG",
                CircuitSettings::setSimplifyStg,
                CircuitSettings::getSimplifyStg));

        properties.add(PropertyHelper.createSeparatorProperty("Gates and arbitration primitives"));

        properties.add(new PropertyDeclaration<>(File.class,
                PropertyHelper.BULLET_PREFIX + GATE_LIBRARY_TITLE,
                value -> setGateLibrary(BackendUtils.getBaseRelativePath(value)),
                () -> BackendUtils.getBaseRelativeFile(getGateLibrary())));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "WAIT name, dirty input and clean handshake",
                value -> {
                    if (value.isEmpty() || parseWaitDataOrNull(value, Wait.Type.WAIT1) != null) {
                        setWaitData(value);
                    } else {
                        errorDescriptionFormat("WAIT", defaultWaitData, true);
                    }
                },
                CircuitSettings::getWaitData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "WAIT0 name, dirty input and clean handshake",
                value -> {
                    if (value.isEmpty() || parseWaitDataOrNull(value, Wait.Type.WAIT0) != null) {
                        setWait0Data(value);
                    } else {
                        errorDescriptionFormat("WAIT0", defaultWait0Data, true);
                    }
                },
                CircuitSettings::getWait0Data));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Early protocol MUTEX name and request-grant pairs",
                value -> {
                    if (value.isEmpty() || (parseMutexDataOrNull(value, Mutex.Protocol.EARLY) != null)) {
                        setMutexData(value);
                    } else {
                        errorDescriptionFormat("MUTEX", defaultMutexData, true);
                    }
                },
                CircuitSettings::getMutexData));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Late protocol MUTEX name and request-grant pairs",
                value -> {
                    if (value.isEmpty() || parseMutexDataOrNull(value, Mutex.Protocol.LATE) != null) {
                        setMutexLateData(value);
                    } else {
                        errorDescriptionFormat("MUTEX", defaultMutexData, true);
                    }
                },
                CircuitSettings::getMutexLateData));

        properties.add(PropertyHelper.createSeparatorProperty("Verilog import and export"));

        properties.add(new PropertyDeclaration<>(File.class,
                PropertyHelper.BULLET_PREFIX + "Substitution rules for export",
                value -> setExportSubstitutionLibrary(BackendUtils.getBaseRelativePath(value)),
                () -> BackendUtils.getBaseRelativeFile(getExportSubstitutionLibrary())));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Invert substitution rules for export",
                CircuitSettings::setInvertExportSubstitutionRules,
                CircuitSettings::getInvertExportSubstitutionRules));

        properties.add(new PropertyDeclaration<>(File.class,
                PropertyHelper.BULLET_PREFIX + "Substitution rules for import",
                value -> setImportSubstitutionLibrary(BackendUtils.getBaseRelativePath(value)),
                () -> BackendUtils.getBaseRelativeFile(getImportSubstitutionLibrary())));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Invert substitution rules for import",
                CircuitSettings::setInvertImportSubstitutionRules,
                CircuitSettings::getInvertImportSubstitutionRules));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Verilog time unit / precision (empty to suppress)",
                CircuitSettings::setVerilogTimescale,
                CircuitSettings::getVerilogTimescale) {
            @Override
            public Map<String, String> getChoice() {
                return PREDEFINED_TIMESCALE_PARAMETERS;
            }
        });

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Delay for assign statements (empty to suppress)",
                CircuitSettings::setVerilogAssignDelay,
                CircuitSettings::getVerilogAssignDelay) {
            @Override
            public Map<String, String> getChoice() {
                return PREDEFINED_DELAY_PARAMETERS;
            }
        });

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Ignore time for WAIT non-persistent input",
                CircuitSettings::setWaitSigIgnoreTime,
                CircuitSettings::getWaitSigIgnoreTime) {
            @Override
            public Map<String, String> getChoice() {
                return PREDEFINED_DELAY_PARAMETERS;
            }
        });

        properties.add(new PropertyDeclaration<>(WaitUndefinedInterpretation.class,
                PropertyHelper.BULLET_PREFIX + "WAIT interpretation of X on non-persistent input",
                CircuitSettings::setWaitUndefinedInterpretation,
                CircuitSettings::getWaitUndefinedInterpretation));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Delay of Early protocol MUTEX grants",
                CircuitSettings::setMutexEarlyGrantDelay,
                CircuitSettings::getMutexEarlyGrantDelay) {
            @Override
            public Map<String, String> getChoice() {
                return PREDEFINED_DELAY_PARAMETERS;
            }
        });

        properties.add(new PropertyDeclaration<>(MutexArbitrationWinner.class,
                PropertyHelper.BULLET_PREFIX + "MUTEX winning grant when both requests are high",
                CircuitSettings::setMutexArbitrationWinner,
                CircuitSettings::getMutexArbitrationWinner));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Bus split/merge suffix on Verilog import/export ("
                        + BUS_INDEX_PLACEHOLDER + " denotes index)",
                CircuitSettings::setBusSuffix,
                CircuitSettings::getBusSuffix));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Dissolve single-bit buses on Verilog export",
                CircuitSettings::setDissolveSingletonBus,
                CircuitSettings::getDissolveSingletonBus));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Accept inout ports and ignore their connections",
                CircuitSettings::setAcceptInoutPort,
                CircuitSettings::getAcceptInoutPort));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "File pattern for import of hierarchical Verilog modules ("
                        + MODULE_NAME_PLACEHOLDER + " denotes module name)",
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
                PropertyHelper.BULLET_PREFIX + "Testable instance prefix (optional "
                        + INITIAL_STATE_PLACEHOLDER + " denotes initial state)",
                CircuitSettings::setTestInstancePrefix,
                CircuitSettings::getTestInstancePrefix));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                PropertyHelper.BULLET_PREFIX + "Use testable instance as path breaker",
                CircuitSettings::setUseTestPathBreaker,
                CircuitSettings::getUseTestPathBreaker));

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

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Auxiliary ports regex to exclude from verification, e.g. sig|req[0-9]*|bus__\\d+",
                CircuitSettings::setAuxiliaryPortRegex,
                CircuitSettings::getAuxiliaryPortRegex));

        properties.add(PropertyHelper.createSeparatorProperty("Buffering of forks with high fanout and scanout output"));

        properties.add(new PropertyDeclaration<>(Integer.class,
                PropertyHelper.BULLET_PREFIX + "Fork high fanout",
                CircuitSettings::setForkHighFanout,
                CircuitSettings::getForkHighFanout));

        properties.add(new PropertyDeclaration<>(String.class,
                PropertyHelper.BULLET_PREFIX + "Fork buffer pattern ("
                        + FORK_FUNOUT_PLACEHOLDER + " denotes fanout)",
                CircuitSettings::setForkBufferPattern,
                CircuitSettings::getForkBufferPattern));

        properties.add(new PropertyDeclaration<>(ScanoutBufferingStyle.class,
                PropertyHelper.BULLET_PREFIX + "Buffering style for scanout output",
                CircuitSettings::setScanoutBufferingStyle,
                CircuitSettings::getScanoutBufferingStyle));
    }

    private static void errorDescriptionFormat(String prefix, String suffix, boolean allowEmpty) {
        String emptyText = allowEmpty ? " either empty or" : "";
        DialogUtils.showError(prefix + " description format is incorrect." +
                "\nIt should be " + emptyText + "as follows:\n" + suffix);
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
        setShowContactFunctions(config.getBoolean(keyShowContactFunctions, defaultShowContactFunctions));
        setShowContactFanout(config.getBoolean(keyShowContactFanout, defaultShowContactFanout));
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
        setMutexLateData(config.getString(keyMutexLateData, defaultMutexLateData));
        // Import/export
        setExportSubstitutionLibrary(config.getString(keyExportSubstitutionLibrary, defaultExportSubstitutionLibrary));
        setInvertExportSubstitutionRules(config.getBoolean(keyInvertExportSubstitutionRules, defaultInvertExportSubstitutionRules));
        setImportSubstitutionLibrary(config.getString(keyImportSubstitutionLibrary, defaultImportSubstitutionLibrary));
        setInvertImportSubstitutionRules(config.getBoolean(keyInvertImportSubstitutionRules, defaultInvertImportSubstitutionRules));
        setVerilogTimescale(config.getString(keyVerilogTimescale, defaultVerilogTimescale));
        setVerilogAssignDelay(config.getString(keyVerilogAssignDelay, defaultVerilogAssignDelay));
        setWaitSigIgnoreTime(config.getString(keyWaitSigIgnoreTime, defaultWaitSigIgnoreTime));
        setWaitUndefinedInterpretation(config.getEnum(keyWaitUndefinedInterpretation, WaitUndefinedInterpretation.class, defaultWaitUndefinedInterpretation));
        setMutexEarlyGrantDelay(config.getString(keyMutexEarlyGrantDelay, defaultMutexEarlyGrantDelay));
        setMutexArbitrationWinner(config.getEnum(keyMutexArbitrationWinner, MutexArbitrationWinner.class, defaultMutexArbitrationWinner));
        setBusSuffix(config.getString(keyBusSuffix, defaultBusSuffix));
        setDissolveSingletonBus(config.getBoolean(keyDissolveSingletonBus, defaultDissolveSingletonBus));
        setAcceptInoutPort(config.getBoolean(keyAcceptInoutPort, defaultAcceptInoutPort));
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
        setUseTestPathBreaker(config.getBoolean(keyUseTestPathBreaker, defaultUseTestPathBreaker));
        setScanSuffix(config.getString(keyScanSuffix, defaultScanSuffix));
        setScaninData(config.getString(keyScaninData, defaultScaninData));
        setScanoutData(config.getString(keyScanoutData, defaultScanoutData));
        setScanenData(config.getString(keyScanenData, defaultScanenData));
        setScanckData(config.getString(keyScanckData, defaultScanckData));
        setScantmData(config.getString(keyScantmData, defaultScantmData));
        setUseIndividualScan(config.getBoolean(keyUseIndividualScan, defaultUseIndividualScan));
        setUseScanInitialisation(config.getBoolean(keyUseScanInitialisation, defaultUseScanInitialisation));
        setInitialisationInverterInstancePrefix(config.getString(keyInitialisationInverterInstancePrefix, defaultInitialisationInverterInstancePrefix));
        setAuxiliaryPortRegex(config.getString(keyAuxiliaryPortRegex, defaultAuxiliaryPortRegex));
        // Forks
        setForkHighFanout(config.getInt(keyForkHighFanout, defaultForkHighFanout));
        setForkBufferPattern(config.getString(keyForkBufferPattern, defaultForkBufferPattern));
        setScanoutBufferingStyle(config.getEnum(keyScanoutBufferingStyle, ScanoutBufferingStyle.class, defaultScanoutBufferingStyle));
    }

    @Override
    public void save(Config config) {
        config.setDouble(keyContactFontSize, getContactFontSize());
        config.setBoolean(keyShowZeroDelayNames, getShowZeroDelayNames());
        config.setBoolean(keyShowContactFunctions, getShowContactFunctions());
        config.setBoolean(keyShowContactFanout, getShowContactFanout());
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
        config.set(keyMutexLateData, getMutexLateData());
        // Import/export
        config.set(keyExportSubstitutionLibrary, getExportSubstitutionLibrary());
        config.setBoolean(keyInvertExportSubstitutionRules, getInvertExportSubstitutionRules());
        config.set(keyImportSubstitutionLibrary, getImportSubstitutionLibrary());
        config.setBoolean(keyInvertImportSubstitutionRules, getInvertImportSubstitutionRules());
        config.set(keyVerilogTimescale, getVerilogTimescale());
        config.set(keyVerilogAssignDelay, getVerilogAssignDelay());
        config.set(keyWaitSigIgnoreTime, getWaitSigIgnoreTime());
        config.setEnum(keyWaitUndefinedInterpretation, getWaitUndefinedInterpretation());
        config.set(keyMutexEarlyGrantDelay, getMutexEarlyGrantDelay());
        config.setEnum(keyMutexArbitrationWinner, getMutexArbitrationWinner());
        config.set(keyBusSuffix, getBusSuffix());
        config.setBoolean(keyDissolveSingletonBus, getDissolveSingletonBus());
        config.setBoolean(keyAcceptInoutPort, getAcceptInoutPort());
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
        config.setBoolean(keyUseTestPathBreaker, getUseTestPathBreaker());
        config.set(keyScanSuffix, getScanSuffix());
        config.set(keyScaninData, getScaninData());
        config.set(keyScanoutData, getScanoutData());
        config.set(keyScanenData, getScanenData());
        config.set(keyScanckData, getScanckData());
        config.set(keyScantmData, getScantmData());
        config.setBoolean(keyUseIndividualScan, getUseIndividualScan());
        config.setBoolean(keyUseScanInitialisation, getUseScanInitialisation());
        config.set(keyInitialisationInverterInstancePrefix, getInitialisationInverterInstancePrefix());
        config.set(keyAuxiliaryPortRegex, getAuxiliaryPortRegex());
        // Forks
        config.setInt(keyForkHighFanout, getForkHighFanout());
        config.set(keyForkBufferPattern, getForkBufferPattern());
        config.setEnum(keyScanoutBufferingStyle, getScanoutBufferingStyle());
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

    public static boolean getShowContactFunctions() {
        return showContactFunctions;
    }

    public static void setShowContactFunctions(boolean value) {
        showContactFunctions = value;
    }

    public static boolean getShowContactFanout() {
        return showContactFanout;
    }

    public static void setShowContactFanout(boolean value) {
        showContactFanout = value;
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
        return parseWaitDataOrNull(type == Wait.Type.WAIT0 ? getWait0Data() : getWaitData(), type);
    }

    public static String getMutexData() {
        return mutexData;
    }

    public static void setMutexData(String value) {
        mutexData = value;
    }

    public static String getMutexLateData() {
        return mutexLateData;
    }

    public static void setMutexLateData(String value) {
        mutexLateData = value;
    }

    public static Mutex parseMutexData(Mutex.Protocol protocol) {
        return parseMutexDataOrNull(protocol == Mutex.Protocol.LATE ? getMutexLateData() : getMutexData(), protocol);
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

    public static String getVerilogTimescale() {
        return verilogTimescale;
    }

    public static void setVerilogTimescale(String value) {
        if (value == null) {
            value = "";
        }
        verilogTimescale = value.trim();
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

    public static String getWaitSigIgnoreTime() {
        return waitSigIgnoreTime;
    }

    public static void setWaitSigIgnoreTime(String value) {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (VerilogUtils.checkAssignDelay(value)) {
            waitSigIgnoreTime = value;
        } else {
            DialogUtils.showError(VerilogUtils.getAssignDelayHelp());
        }
    }

    public static WaitUndefinedInterpretation getWaitUndefinedInterpretation() {
        return waitUndefinedInterpretation;
    }

    public static void setWaitUndefinedInterpretation(WaitUndefinedInterpretation value) {
        waitUndefinedInterpretation = value;
    }

    public static String getMutexEarlyGrantDelay() {
        return mutexEarlyGrantDelay;
    }

    public static void setMutexEarlyGrantDelay(String value) {
        if (value == null) {
            value = "";
        }
        value = value.trim();
        if (VerilogUtils.checkAssignDelay(value)) {
            mutexEarlyGrantDelay = value;
        } else {
            DialogUtils.showError(VerilogUtils.getAssignDelayHelp());
        }
    }

    public static MutexArbitrationWinner getMutexArbitrationWinner() {
        return mutexArbitrationWinner;
    }

    public static void setMutexArbitrationWinner(MutexArbitrationWinner value) {
        mutexArbitrationWinner = value;
    }

    public static String getBusSuffix() {
        return busSuffix;
    }

    public static void setBusSuffix(String value) {
        if ((value == null) || !value.contains(BUS_INDEX_PLACEHOLDER)) {
            DialogUtils.showError("Bus suffix must have index placeholder " + BUS_INDEX_PLACEHOLDER);
        } else {
            busSuffix = value;
        }
    }

    public static boolean getDissolveSingletonBus() {
        return dissolveSingletonBus;
    }

    public static void setDissolveSingletonBus(boolean value) {
        dissolveSingletonBus = value;
    }

    public static boolean getAcceptInoutPort() {
        return acceptInoutPort;
    }

    public static void setAcceptInoutPort(boolean value) {
        acceptInoutPort = value;
    }

    public static String getModuleFilePattern() {
        return moduleFilePattern;
    }

    public static void setModuleFilePattern(String value) {
        if ((value == null) || !value.contains(MODULE_NAME_PLACEHOLDER)) {
            DialogUtils.showError("File pattern must have module name placeholder " + MODULE_NAME_PLACEHOLDER);
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

    public static String getTestInstanceName(boolean initToOne) {
        String pattern = getTestInstancePrefix();
        if (pattern == null) {
            pattern = defaultTestInstancePrefix;
        }
        return pattern.replace(INITIAL_STATE_PLACEHOLDER, initToOne ? "1" : "0");
    }

    public static void setTestInstancePrefix(String value) {
        testInstancePrefix = value;
    }

    public static boolean getUseTestPathBreaker() {
        return useTestPathBreaker;
    }

    public static void setUseTestPathBreaker(boolean value) {
        useTestPathBreaker = value;
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

    public static String getAuxiliaryPortRegex() {
        return auxiliaryPortRegex;
    }

    public static boolean isAuxiliaryPortName(String name) {
        Pattern exceptionPattern = Pattern.compile(getAuxiliaryPortRegex());
        Matcher exceptionMatcher = exceptionPattern.matcher(name);
        return exceptionMatcher.matches();
    }

    public static void setAuxiliaryPortRegex(String value) {
        auxiliaryPortRegex = value;
    }

    public static String getForkBufferPattern() {
        return forkBufferPattern;
    }

    public static void setForkBufferPattern(String value) {
        if ((value == null) || !value.contains(FORK_FUNOUT_PLACEHOLDER)) {
            DialogUtils.showError("Fork buffer pattern must have fanout placeholder " + FORK_FUNOUT_PLACEHOLDER);
        } else {
            forkBufferPattern = value;
        }
    }

    public static boolean isForkBufferReference(String ref) {
        Pattern pattern = Pattern.compile('^' + getForkBufferPattern()
                .replace(FORK_FUNOUT_PLACEHOLDER, "(\\d+)"));

        String name = NamespaceHelper.getReferenceName(Identifier.truncateNamespaceSeparator(ref));
        return pattern.matcher(name).find();
    }

    public static int getForkHighFanout() {
        return forkHighFanout;
    }

    public static void setForkHighFanout(int value) {
        if (value < 2) {
            DialogUtils.showError("Fork fanout must be greater than 1");
        } else {
            forkHighFanout = value;
        }
    }

    public static String getForkBufferName(int fanout) {
        String pattern = getForkBufferPattern();
        if ((pattern == null) || !pattern.contains(FORK_FUNOUT_PLACEHOLDER)) {
            pattern = defaultForkBufferPattern;
        }
        return pattern.replace(FORK_FUNOUT_PLACEHOLDER, Integer.toString(fanout));
    }

    public static ScanoutBufferingStyle getScanoutBufferingStyle() {
        return scanoutBufferingStyle;
    }

    public static void setScanoutBufferingStyle(ScanoutBufferingStyle value) {
        scanoutBufferingStyle = value;
    }

    private static void setGate2Data(String value, Consumer<String> setter, String msg, String defaultValue) {
        if (parseGate2DataOrNull(value) != null) {
            setter.accept(value);
        } else {
            errorDescriptionFormat(msg, defaultValue, false);
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

    private static Mutex parseMutexDataOrNull(String str, Mutex.Protocol protocol) {
        Mutex result = null;
        Matcher matcher = MUTEX_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(MUTEX_NAME_GROUP);
            Signal r1 = new Signal(matcher.group(MUTEX_R1_GROUP), Signal.Type.INPUT);
            Signal g1 = new Signal(matcher.group(MUTEX_G1_GROUP), Signal.Type.OUTPUT);
            Signal r2 = new Signal(matcher.group(MUTEX_R2_GROUP), Signal.Type.INPUT);
            Signal g2 = new Signal(matcher.group(MUTEX_G2_GROUP), Signal.Type.OUTPUT);
            result = new Mutex(name, r1, g1, r2, g2, protocol);
        }
        return result;
    }

    private static Wait parseWaitDataOrNull(String str, Wait.Type type) {
        Wait result = null;
        Matcher matcher = WAIT_DATA_PATTERN.matcher(str.replaceAll("\\s", ""));
        if (matcher.find()) {
            String name = matcher.group(WAIT_NAME_GROUP);
            Signal sig = new Signal(matcher.group(WAIT_SIG_GROUP), Signal.Type.INPUT);
            Signal ctrl = new Signal(matcher.group(WAIT_CTRL_GROUP), Signal.Type.INPUT);
            Signal san = new Signal(matcher.group(WAIT_SAN_GROUP), Signal.Type.OUTPUT);
            result = new Wait(name, type, sig, ctrl, san);
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

    public static Pattern getBusSignalPattern(String name) {
        String busSuffix = getProcessedBusSuffix();
        return Pattern.compile(name + busSuffix.replace(BUS_INDEX_PLACEHOLDER, "(\\d+)"));
    }

    public static Pattern getBusSignalPattern() {
        String busSuffix = getProcessedBusSuffix();
        return Pattern.compile("(.+)" + busSuffix.replace(BUS_INDEX_PLACEHOLDER, "(\\d+)"));
    }

    public static String getProcessedBusSuffix(String replacement) {
        return getProcessedBusSuffix().replace(BUS_INDEX_PLACEHOLDER, replacement);
    }

    private static String getProcessedBusSuffix() {
        String result = getBusSuffix();
        if (result == null) {
            result = BUS_INDEX_PLACEHOLDER;
        }
        if (!result.contains(BUS_INDEX_PLACEHOLDER)) {
            result += BUS_INDEX_PLACEHOLDER;
        }
        return result;
    }

    public static String getModuleFileName(String moduleName) {
        return getProcessedModuleFilePattern().replace(MODULE_NAME_PLACEHOLDER, moduleName);
    }

    private static String getProcessedModuleFilePattern() {
        String result = getModuleFilePattern();
        if (result == null) {
            result = MODULE_NAME_PLACEHOLDER;
        }
        if (!result.contains(MODULE_NAME_PLACEHOLDER)) {
            result = MODULE_NAME_PLACEHOLDER + result;
        }
        return result;
    }

}
