package org.workcraft.plugins.shutters;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.PropertyDeclaration;
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

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "Ltscat module path",
                ShuttersSettings::setLtscatFolder,
                ShuttersSettings::getLtscatFolder));

        properties.add(new PropertyDeclaration<>(String.class,
                "Shutters command",
                ShuttersSettings::setShuttersCommand,
                ShuttersSettings::getShuttersCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Espresso command",
                ShuttersSettings::setEspressoCommand,
                ShuttersSettings::getEspressoCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Abc command",
                ShuttersSettings::setAbcCommand,
                ShuttersSettings::getAbcCommand));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Force positive literals",
                ShuttersSettings::setForcePositiveMode,
                ShuttersSettings::getForcePositiveMode));
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
