package org.workcraft.plugins.wtg;

import java.util.Collection;
import java.util.LinkedList;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class WaverSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "WaverSettings";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\Waver\\waver.exe" : "tools/Waver/waver";
    private static final String defaultArgs = "";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;

    public WaverSettings() {
        properties.add(new PropertyDeclaration<WaverSettings, String>(
                this, "Waver command", String.class, true, false, false) {
            protected void setter(WaverSettings object, String value) {
                setCommand(value);
            }
            protected String getter(WaverSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<WaverSettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(WaverSettings object, String value) {
                setArgs(value);
            }
            protected String getter(WaverSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<WaverSettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(WaverSettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(WaverSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<WaverSettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(WaverSettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(WaverSettings object) {
                return getPrintStderr();
            }
        });
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Waver";
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
