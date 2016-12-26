package org.workcraft.plugins.shared;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class CommonDebugSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "CommonDebugSettings";

    private static final String keyCopyModelOnChange = prefix + ".copyModelOnChange";
    private static final String keyVerboseImport = prefix + ".verboseImport";
    private static final String keyParserTracing = prefix + ".parserTracing";
    private static final String keyVerboseCompatibilityManager = prefix + ".verboseCompatibilityManager";

    private static final boolean defaultCopyModelOnChange = false;
    private static final Boolean defaultVerboseImport = false;
    private static final Boolean defaultParserTracing = false;
    private static final Boolean defaultVerboseCompatibilityManager = false;

    private static boolean copyModelOnChange = defaultCopyModelOnChange;
    private static Boolean verboseImport = defaultVerboseImport;
    private static Boolean parserTracing = defaultParserTracing;
    private static Boolean verboseCompatibilityManager = defaultVerboseCompatibilityManager;

    public CommonDebugSettings() {
        properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
                this, "On modifications copy model to clipboard", Boolean.class, true, false, false) {
            protected void setter(CommonDebugSettings object, Boolean value) {
                setCopyModelOnChange(value);
            }
            protected Boolean getter(CommonDebugSettings object) {
                return getCopyModelOnChange();
            }
        });

        properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
                this, "Verbose log on file import", Boolean.class, true, false, false) {
            protected void setter(CommonDebugSettings object, Boolean value) {
                setVerboseImport(value);
            }
            protected Boolean getter(CommonDebugSettings object) {
                return getVerboseImport();
            }
        });

        properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
                this, "Log tracing information from parsers", Boolean.class, true, false, false) {
            protected void setter(CommonDebugSettings object, Boolean value) {
                setParserTracing(value);
            }
            protected Boolean getter(CommonDebugSettings object) {
                return getParserTracing();
            }
        });

        properties.add(new PropertyDeclaration<CommonDebugSettings, Boolean>(
                this, "Log compatibility manager substitutions", Boolean.class, true, false, false) {
            protected void setter(CommonDebugSettings object, Boolean value) {
                setVerboseCompatibilityManager(value);
            }
            protected Boolean getter(CommonDebugSettings object) {
                return getVerboseCompatibilityManager();
            }
        });
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
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyCopyModelOnChange, getCopyModelOnChange());
        config.setBoolean(keyVerboseImport, getVerboseImport());
        config.setBoolean(keyParserTracing, getParserTracing());
        config.setBoolean(keyVerboseCompatibilityManager, getVerboseCompatibilityManager());
    }

    @Override
    public String getSection() {
        return "Common";
    }

    @Override
    public String getName() {
        return "Debug";
    }

    public static Boolean getCopyModelOnChange() {
        return copyModelOnChange;
    }

    public static void setCopyModelOnChange(Boolean value) {
        copyModelOnChange = value;
    }

    public static Boolean getVerboseImport() {
        return verboseImport;
    }

    public static void setVerboseImport(Boolean value) {
        verboseImport = value;
    }

    public static Boolean getParserTracing() {
        return parserTracing;
    }

    public static void setParserTracing(Boolean value) {
        parserTracing = value;
    }

    public static Boolean getVerboseCompatibilityManager() {
        return verboseCompatibilityManager;
    }

    public static void setVerboseCompatibilityManager(Boolean value) {
        verboseCompatibilityManager = value;
    }

}
