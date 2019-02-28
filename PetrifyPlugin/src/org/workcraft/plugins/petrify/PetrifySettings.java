package org.workcraft.plugins.petrify;

import org.workcraft.Config;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;

import java.util.LinkedList;
import java.util.List;

public class PetrifySettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.petrify";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyWriteLog = prefix + ".writeLog";
    private static final String keyWriteStg = prefix + ".writeStg";
    private static final String keyWriteEqn = prefix + ".writeEqn";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyOpenSynthesisStg = prefix + ".openSynthesisStg";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\PetrifyTools\\petrify.exe" : "tools/PetrifyTools/petrify";
    private static final String defaultArgs = "";
    private static final boolean defaultAdvancedMode = false;
    private static final boolean defaultWriteLog = true;
    private static final boolean defaultWriteStg = false;
    private static final boolean defaultWriteEqn = false;
    private static final boolean defaultPrintStdout = true;
    private static final boolean defaultPrintStderr = true;
    private static final boolean defaultOpenSynthesisStg = false;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static boolean advancedMode = defaultAdvancedMode;
    private static boolean writeLog = defaultWriteLog;
    private static boolean writeStg = defaultWriteStg;
    private static boolean writeEqn = defaultWriteEqn;
    private static boolean printStdout = defaultPrintStdout;
    private static boolean printStderr = defaultPrintStderr;
    private static boolean openSynthesisStg = defaultOpenSynthesisStg;

    public PetrifySettings() {
        properties.add(new PropertyDeclaration<PetrifySettings, String>(
                this, "Petrify command", String.class) {
            @Override
            public void setter(PetrifySettings object, String value) {
                setCommand(value);
            }
            @Override
            public String getter(PetrifySettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, String>(
                this, "Additional parameters", String.class) {
            @Override
            public void setter(PetrifySettings object, String value) {
                setArgs(value);
            }
            @Override
            public String getter(PetrifySettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setAdvancedMode(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Write log file (" + PetrifyUtils.LOG_FILE_NAME + ")", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setWriteLog(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getWriteLog();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Write STG file (" + PetrifyUtils.STG_FILE_NAME + ")", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setWriteStg(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getWriteStg();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Write EQN file (" + PetrifyUtils.EQN_FILE_NAME + ")", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setWriteEqn(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getWriteEqn();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Output stdout", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setPrintStdout(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Output stderr", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setPrintStderr(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<PetrifySettings, Boolean>(
                this, "Open resulting STG if new signals are inserted", Boolean.class) {
            @Override
            public void setter(PetrifySettings object, Boolean value) {
                setOpenSynthesisStg(value);
            }
            @Override
            public Boolean getter(PetrifySettings object) {
                return getOpenSynthesisStg();
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
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setWriteLog(config.getBoolean(keyWriteLog, defaultWriteLog));
        setWriteStg(config.getBoolean(keyWriteStg, defaultWriteStg));
        setWriteEqn(config.getBoolean(keyWriteEqn, defaultWriteEqn));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setOpenSynthesisStg(config.getBoolean(keyOpenSynthesisStg, defaultOpenSynthesisStg));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyWriteLog, getWriteLog());
        config.setBoolean(keyWriteStg, getWriteStg());
        config.setBoolean(keyWriteEqn, getWriteEqn());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyOpenSynthesisStg, getOpenSynthesisStg());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "Petrify";
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

    public static Boolean getWriteLog() {
        return writeLog;
    }

    public static void setWriteLog(Boolean value) {
        writeLog = value;
    }

    public static Boolean getWriteStg() {
        return writeStg;
    }

    public static void setWriteStg(Boolean value) {
        writeStg = value;
    }

    public static Boolean getWriteEqn() {
        return writeEqn;
    }

    public static void setWriteEqn(Boolean value) {
        writeEqn = value;
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

    public static boolean getOpenSynthesisStg() {
        return openSynthesisStg;
    }

    public static void setOpenSynthesisStg(boolean value) {
        openSynthesisStg = value;
    }

}
