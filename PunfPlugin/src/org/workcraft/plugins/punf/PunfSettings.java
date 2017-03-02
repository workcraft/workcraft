package org.workcraft.plugins.punf;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class PunfSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.punf";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyUsePnmlUnfolding = prefix + ".usePnmlUnfolding";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\punf.exe" : "tools/UnfoldingTools/punf";
    private static final String defaultArgs = "-r";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultUsePnmlUnfolding = true;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean usePnmlUnfolding = defaultUsePnmlUnfolding;

    public PunfSettings() {
        properties.add(new PropertyDeclaration<PunfSettings, String>(
                this, "Punf command", String.class, true, false, false) {
            protected void setter(PunfSettings object, String value) {
                setCommand(value);
            }
            protected String getter(PunfSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<PunfSettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(PunfSettings object, String value) {
                setArgs(value);
            }
            protected String getter(PunfSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<PunfSettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(PunfSettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(PunfSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<PunfSettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(PunfSettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(PunfSettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<PunfSettings, Boolean>(
                this, "Use PNML-based unfolding (where possible)", Boolean.class, true, false, false) {
            protected void setter(PunfSettings object, Boolean value) {
                setUsePnmlUnfolding(value);
            }
            protected Boolean getter(PunfSettings object) {
                return getUsePnmlUnfolding();
            }
        });
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setUsePnmlUnfolding(config.getBoolean(keyUsePnmlUnfolding, defaultUsePnmlUnfolding));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyUsePnmlUnfolding, getUsePnmlUnfolding());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Punf";
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

    public static Boolean getUsePnmlUnfolding() {
        return usePnmlUnfolding;
    }

    public static void setUsePnmlUnfolding(Boolean value) {
        usePnmlUnfolding = value;
    }

    public static String getUnfoldingExtension(boolean tryPnml) {
        return tryPnml && getUsePnmlUnfolding() ? ".pnml" : ".mci";
    }

    public static String getToolSuffix(boolean tryPnml) {
        return tryPnml && getUsePnmlUnfolding() ? "" : "-mci";
    }

}
