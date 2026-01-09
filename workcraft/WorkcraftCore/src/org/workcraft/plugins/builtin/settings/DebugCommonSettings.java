package org.workcraft.plugins.builtin.settings;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;

import java.util.LinkedList;
import java.util.List;

public class DebugCommonSettings extends AbstractCommonSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "CommonDebugSettings";

    private static final String keyVerboseImport = prefix + ".verboseImport";
    private static final String keyParserTracing = prefix + ".parserTracing";
    private static final String keyVerboseCompatibilityManager = prefix + ".verboseCompatibilityManager";
    private static final String keyLodPerformance = prefix + ".logPerformance";

    private static final boolean defaultVerboseImport = false;
    private static final boolean defaultParserTracing = false;
    private static final boolean defaultVerboseCompatibilityManager = false;
    private static final boolean defaultLogPerformance = false;

    private static boolean verboseImport = defaultVerboseImport;
    private static boolean parserTracing = defaultParserTracing;
    private static boolean verboseCompatibilityManager = defaultVerboseCompatibilityManager;
    private static boolean logPerformance = defaultLogPerformance;

    static {
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
                "Enable popup menu for logging performance",
                DebugCommonSettings::setLogPerformance,
                DebugCommonSettings::getLogPerformance));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setVerboseImport(config.getBoolean(keyVerboseImport, defaultVerboseImport));
        setParserTracing(config.getBoolean(keyParserTracing, defaultParserTracing));
        setVerboseCompatibilityManager(config.getBoolean(keyVerboseCompatibilityManager, defaultVerboseCompatibilityManager));
        setLogPerformance(config.getBoolean(keyLodPerformance, defaultLogPerformance));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyVerboseImport, getVerboseImport());
        config.setBoolean(keyParserTracing, getParserTracing());
        config.setBoolean(keyVerboseCompatibilityManager, getVerboseCompatibilityManager());
        config.setBoolean(keyLodPerformance, getLogPerformance());
    }

    @Override
    public String getName() {
        return "Debug";
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

    public static boolean getLogPerformance() {
        return logPerformance;
    }

    public static void setLogPerformance(boolean value) {
        logPerformance = value;
    }

}
