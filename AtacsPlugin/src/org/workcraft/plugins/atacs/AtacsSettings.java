package org.workcraft.plugins.atacs;

import org.workcraft.Config;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.util.LinkedList;
import java.util.List;

public class AtacsSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.atacs";

    private static final String keyShowInMenu = prefix + ".showInMenu";
    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";

    private static final Boolean defaultShowInMenu = false;
    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\ATACS\\atacs.exe" : "tools/ATACS/atacs";
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;

    private static Boolean showInMenu = defaultShowInMenu;
    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;

    public AtacsSettings() {
        properties.add(new PropertyDeclaration<AtacsSettings, Boolean>(
                this, "Activate ATACS synthesis (experimental)", Boolean.class) {
            @Override
            public void setter(AtacsSettings object, Boolean value) {
                setShowInMenu(value);
            }
            @Override
            public Boolean getter(AtacsSettings object) {
                return getShowInMenu();
            }
        });

        properties.add(new PropertyDeclaration<AtacsSettings, String>(
                this, "ATACS command", String.class) {
            @Override
            public void setter(AtacsSettings object, String value) {
                setCommand(value);
            }
            @Override
            public String getter(AtacsSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<AtacsSettings, String>(
                this, "Additional parameters", String.class) {
            @Override
            public void setter(AtacsSettings object, String value) {
                setArgs(value);
            }
            @Override
            public String getter(AtacsSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<AtacsSettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class) {
            @Override
            public void setter(AtacsSettings object, Boolean value) {
                setAdvancedMode(value);
            }
            @Override
            public Boolean getter(AtacsSettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<AtacsSettings, Boolean>(
                this, "Output stdout", Boolean.class) {
            @Override
            public void setter(AtacsSettings object, Boolean value) {
                setPrintStdout(value);
            }
            @Override
            public Boolean getter(AtacsSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<AtacsSettings, Boolean>(
                this, "Output stderr", Boolean.class) {
            @Override
            public void setter(AtacsSettings object, Boolean value) {
                setPrintStderr(value);
            }
            @Override
            public Boolean getter(AtacsSettings object) {
                return getPrintStderr();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setShowInMenu(config.getBoolean(keyShowInMenu, defaultShowInMenu));
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowInMenu, getShowInMenu());
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "ATACS";
    }

    public static boolean getShowInMenu() {
        return showInMenu;
    }

    public static void setShowInMenu(boolean value) {
        showInMenu = value;
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String value) {
        command = value;
    }

    public static String getArgs() {
        return args;
    }

    public static void setArgs(String value) {
        args = value;
    }

    public static Boolean getAdvancedMode() {
        return advancedMode;
    }

    public static void setAdvancedMode(Boolean value) {
        advancedMode = value;
    }

    public static Boolean getPrintStdout() {
        return printStdout;
    }

    public static void setPrintStdout(Boolean value) {
        printStdout = value;
    }

    public static Boolean getPrintStderr() {
        return printStderr;
    }

    public static void setPrintStderr(Boolean value) {
        printStderr = value;
    }

}
