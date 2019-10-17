package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyDeclaration;

import java.util.LinkedList;
import java.util.List;

public class DebugCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonDebugSettings";

    private static final String keyCopyModelOnChange = prefix + ".copyModelOnChange";
    private static final String keyVerboseImport = prefix + ".verboseImport";
    private static final String keyParserTracing = prefix + ".parserTracing";
    private static final String keyVerboseCompatibilityManager = prefix + ".verboseCompatibilityManager";
    private static final String keyShortExportHeader = prefix + ".shortExportHeader";

    private static final boolean defaultCopyModelOnChange = false;
    private static final boolean defaultVerboseImport = false;
    private static final boolean defaultParserTracing = false;
    private static final boolean defaultVerboseCompatibilityManager = false;
    private static final boolean defaultShortExportHeader = false;

    private static boolean copyModelOnChange = defaultCopyModelOnChange;
    private static boolean verboseImport = defaultVerboseImport;
    private static boolean parserTracing = defaultParserTracing;
    private static boolean verboseCompatibilityManager = defaultVerboseCompatibilityManager;
    private static boolean shortExportHeader = defaultShortExportHeader;

    static {
        properties.add(new PropertyDeclaration<>(Boolean.class,
                "On modifications copy model to clipboard",
                DebugCommonSettings::setCopyModelOnChange,
                DebugCommonSettings::getCopyModelOnChange));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Verbose log on file import",
                DebugCommonSettings::setVerboseImport,
                DebugCommonSettings::getVerboseImport));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Log tracing information from parsers",
                DebugCommonSettings::setParserTracing,
                DebugCommonSettings::getParserTracing));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Log compatibility manager substitutions",
                DebugCommonSettings::setVerboseCompatibilityManager,
                DebugCommonSettings::getVerboseCompatibilityManager));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Use short header in exported files",
                DebugCommonSettings::setShortExportHeader,
                DebugCommonSettings::getShortExportHeader));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCopyModelOnChange(config.getBoolean(keyCopyModelOnChange, defaultCopyModelOnChange));
        setVerboseImport(config.getBoolean(keyVerboseImport, defaultVerboseImport));
        setParserTracing(config.getBoolean(keyParserTracing, defaultParserTracing));
        setVerboseCompatibilityManager(config.getBoolean(keyVerboseCompatibilityManager, defaultVerboseCompatibilityManager));
        setShortExportHeader(config.getBoolean(keyShortExportHeader, defaultShortExportHeader));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyCopyModelOnChange, getCopyModelOnChange());
        config.setBoolean(keyVerboseImport, getVerboseImport());
        config.setBoolean(keyParserTracing, getParserTracing());
        config.setBoolean(keyVerboseCompatibilityManager, getVerboseCompatibilityManager());
        config.setBoolean(keyShortExportHeader, getShortExportHeader());
    }

    @Override
    public String getName() {
        return "Debug";
    }

    public static boolean getCopyModelOnChange() {
        return copyModelOnChange;
    }

    public static void setCopyModelOnChange(boolean value) {
        copyModelOnChange = value;
    }

    public static boolean getVerboseImport() {
        return verboseImport;
    }

    public static void setVerboseImport(boolean value) {
        verboseImport = value;
    }

    public static boolean getParserTracing() {
        return parserTracing;
    }

    public static void setParserTracing(boolean value) {
        parserTracing = value;
    }

    public static boolean getVerboseCompatibilityManager() {
        return verboseCompatibilityManager;
    }

    public static void setVerboseCompatibilityManager(boolean value) {
        verboseCompatibilityManager = value;
    }

    public static boolean getShortExportHeader() {
        return shortExportHeader;
    }

    public static void setShortExportHeader(boolean value) {
        shortExportHeader = value;
    }

}
