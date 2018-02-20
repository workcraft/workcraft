package org.workcraft.plugins.pcomp;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PcompSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.pcomp";

    private static final String ketCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\pcomp.exe" : "tools/UnfoldingTools/pcomp";
    private static final String defaultArgs = "";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;

    public PcompSettings() {
        properties.add(new PropertyDeclaration<PcompSettings, String>(
                this, "PComp command", String.class, true, false, false) {
            protected void setter(PcompSettings object, String value) {
                setCommand(value);
            }
            protected String getter(PcompSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<PcompSettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(PcompSettings object, String value) {
                setArgs(value);
            }
            protected String getter(PcompSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<PcompSettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(PcompSettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(PcompSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<PcompSettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(PcompSettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(PcompSettings object) {
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
        setCommand(config.getString(ketCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.set(ketCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "PComp";
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
