package org.workcraft.plugins.shutters;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.DesktopApi;

import java.util.Collection;
import java.util.LinkedList;

public class ShuttersSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "ShuttersSettings";

    private static final String keyShuttersCommand = prefix + ".shuttersCommand";
    private static final String keyLtscatFolder = prefix + ".ltscatfolder";
    private static final String keyForcePositive = prefix + ".forcePositiveMode";
    private static final String keyEspressoCommand = prefix + ".espressoCommand";
    private static final String keyAbcCommand = prefix + ".defaultAbcCommand";

    private static final String defaultShuttersCommand = DesktopApi.getOs().isWindows() ? "tools\\Process-Windows\\Shutters\\shutters.exe" : "tools/Process-Windows/Shutters/shutters";
    private static final String defaultLtscatFolder = DesktopApi.getOs().isWindows() ? "tools\\Process-Windows\\Ltscat" : "tools/Process-Windows/Ltscat";
    private static final String defaultEspressoCommand = DesktopApi.getOs().isWindows() ? "tools\\Espresso\\espresso.exe" : "tools/Espresso/espresso";
    private static final String defaultAbcCommand = DesktopApi.getOs().isWindows() ? "abc.exe" : "abc";
    private static final Boolean defaultForcePositiveMode = false;

    private static final String exportedFstExtension = ".sg";
    private static final String windowsExtension = ".pn";
    private static final String markingsExtension = ".markings";
    private static final String ltscatModuleName = "pyLtsCat";
    private static final String scriptName = "script.py";
    private static final String python3Command = "python3";

    private static String shuttersCommand = defaultShuttersCommand;
    private static String ltscatFolder = defaultLtscatFolder;
    private static Boolean forcePositiveMode = defaultForcePositiveMode;
    private static String espressoCommand = defaultEspressoCommand;
    private static String abcCommand = defaultAbcCommand;

    public ShuttersSettings() {

        properties.add(new PropertyDeclaration<ShuttersSettings, String>(
                this, "Ltscat module path", String.class) {
            @Override
            public void setter(ShuttersSettings object, String value) {
                setLtscatFolder(value);
            }
            @Override
            public String getter(ShuttersSettings object) {
                return getLtscatFolder();
            }
        });

        properties.add(new PropertyDeclaration<ShuttersSettings, String>(
                this, "Shutters command", String.class) {
            @Override
            public void setter(ShuttersSettings object, String value) {
                setShuttersCommand(value);
            }
            @Override
            public String getter(ShuttersSettings object) {
                return getShuttersCommand();
            }
        });

        properties.add(new PropertyDeclaration<ShuttersSettings, String>(
                this, "Espresso command", String.class) {
            @Override
            public void setter(ShuttersSettings object, String value) {
                setEspressoCommand(value);
            }
            @Override
            public String getter(ShuttersSettings object) {
                return getEspressoCommand();
            }
        });

        properties.add(new PropertyDeclaration<ShuttersSettings, String>(
                this, "Abc command", String.class) {
            @Override
            public void setter(ShuttersSettings object, String value) {
                setAbcCommand(value);
            }
            @Override
            public String getter(ShuttersSettings object) {
                return getAbcCommand();
            }
        });

        properties.add(new PropertyDeclaration<ShuttersSettings, Boolean>(
                this, "Force positive literals", Boolean.class) {
            @Override
            public void setter(ShuttersSettings object, Boolean value) {
                setForcePositiveMode(value);
            }
            @Override
            public Boolean getter(ShuttersSettings object) {
                return getForcePositiveMode();
            }
        });
    }

    @Override
    public void load(Config config) {
        setShuttersCommand(config.getString(keyShuttersCommand, defaultShuttersCommand));
        setLtscatFolder(config.getString(keyLtscatFolder, defaultLtscatFolder));
        setForcePositiveMode(config.getBoolean(keyForcePositive, defaultForcePositiveMode));
        setEspressoCommand(config.getString(keyEspressoCommand, defaultEspressoCommand));
        setAbcCommand(config.getString(keyAbcCommand, defaultAbcCommand));
    }

    @Override
    public void save(Config config) {
        config.set(keyShuttersCommand, getShuttersCommand());
        config.set(keyLtscatFolder, getLtscatFolder());
        config.setBoolean(keyForcePositive, getForcePositiveMode());
        config.set(keyEspressoCommand, getEspressoCommand());
        config.set(keyAbcCommand, getAbcCommand());
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getName() {
        return "Process windows";
    }

    public static String getShuttersCommand() {
        return shuttersCommand;
    }

    public static void setShuttersCommand(String value) {
        shuttersCommand = value;
    }

    public static String getLtscatFolder() {
        return ltscatFolder;
    }

    public static void setLtscatFolder(String value) {
        ltscatFolder = value;
    }

    public static Boolean getForcePositiveMode() {
        return forcePositiveMode;
    }

    public static void setForcePositiveMode(Boolean value) {
        forcePositiveMode = value;
    }

    public static String getWindowsExtension() {
        return windowsExtension;
    }

    public static String getLtscatModuleName() {
        return ltscatModuleName;
    }

    public static String getScriptName() {
        return scriptName;
    }

    public static String getExportedFstExtension() {
        return exportedFstExtension;
    }

    public static String getPython3Command() {
        return python3Command;
    }

    public static String getMarkingsExtension() {
        return markingsExtension;
    }

    public static String getEspressoCommand() {
        return espressoCommand;
    }

    public static void setEspressoCommand(String espressoCommand) {
        ShuttersSettings.espressoCommand = espressoCommand;
    }

    public static String getAbcCommand() {
        return abcCommand;
    }

    public static void setAbcCommand(String abcCommand) {
        ShuttersSettings.abcCommand = abcCommand;
    }
}
