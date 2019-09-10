package org.workcraft.plugins.msfsm;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.DesktopApi;

import java.util.LinkedList;
import java.util.List;

public class MsfsmSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.msfsm";

    private static final String keyShowInMenu = prefix + ".showInMenu";
    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";

    private static final Boolean defaultShowInMenu = false;
    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\MSFSM\\msfsm_tools.exe" : "tools/MSFSM/msfsm_tools";
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

    public MsfsmSettings() {
        properties.add(new PropertyDeclaration<MsfsmSettings, Boolean>(
                this, "Activate MSFSM backend (experimental) - requires restart", Boolean.class) {
            @Override
            public void setter(MsfsmSettings object, Boolean value) {
                setShowInMenu(value);
            }
            @Override
            public Boolean getter(MsfsmSettings object) {
                return getShowInMenu();
            }
        });

        properties.add(new PropertyDeclaration<MsfsmSettings, String>(
                this, "MSFSM command", String.class) {
            @Override
            public void setter(MsfsmSettings object, String value) {
                setCommand(value);
            }
            @Override
            public String getter(MsfsmSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<MsfsmSettings, String>(
                this, "Additional parameters", String.class) {
            @Override
            public void setter(MsfsmSettings object, String value) {
                setArgs(value);
            }
            @Override
            public String getter(MsfsmSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<MsfsmSettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class) {
            @Override
            public void setter(MsfsmSettings object, Boolean value) {
                setAdvancedMode(value);
            }
            @Override
            public Boolean getter(MsfsmSettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<MsfsmSettings, Boolean>(
                this, "Output stdout", Boolean.class) {
            @Override
            public void setter(MsfsmSettings object, Boolean value) {
                setPrintStdout(value);
            }
            @Override
            public Boolean getter(MsfsmSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<MsfsmSettings, Boolean>(
                this, "Output stderr", Boolean.class) {
            @Override
            public void setter(MsfsmSettings object, Boolean value) {
                setPrintStderr(value);
            }
            @Override
            public Boolean getter(MsfsmSettings object) {
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
    public String getName() {
        return "MSFSM";
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
