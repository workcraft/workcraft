package org.workcraft.plugins.mpsat;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.Config;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.gui.propertyeditor.Settings;

public class MpsatSynthesisSettings implements Settings {
    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatSynthesis";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyOpenSynthesisResult = prefix + ".openSynthesisResult";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\mpsat.exe" : "tools/UnfoldingTools/mpsat";
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final boolean defaultOpenSynthesisResult = true;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static boolean openSynthesisResult = defaultOpenSynthesisResult;

    public MpsatSynthesisSettings() {
        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, String>(
                this, "MPSat command for synthesis", String.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, String value) {
                setCommand(value);
            }
            protected String getter(MpsatSynthesisSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, String>(
                this, "Additional parameters", String.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, String value) {
                setArgs(value);

            }
            protected String getter(MpsatSynthesisSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, Boolean value) {
                setAdvancedMode(value);
            }
            protected Boolean getter(MpsatSynthesisSettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, Boolean>(
                this, "Output stdout", Boolean.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, Boolean value) {
                setPrintStdout(value);
            }
            protected Boolean getter(MpsatSynthesisSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, Boolean>(
                this, "Output stderr", Boolean.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, Boolean value) {
                setPrintStderr(value);
            }
            protected Boolean getter(MpsatSynthesisSettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<MpsatSynthesisSettings, Boolean>(
                this, "Open synthesis result as Digital Circuit", Boolean.class, true, false, false) {
            protected void setter(MpsatSynthesisSettings object, Boolean value) {
                setOpenSynthesisResult(value);
            }
            protected Boolean getter(MpsatSynthesisSettings object) {
                return getOpenSynthesisResult();
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
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setOpenSynthesisResult(config.getBoolean(keyOpenSynthesisResult, defaultOpenSynthesisResult));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyOpenSynthesisResult, getOpenSynthesisResult());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "MPSat synthesis";
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

    public static boolean getOpenSynthesisResult() {
        return openSynthesisResult;
    }

    public static void setOpenSynthesisResult(boolean value) {
        openSynthesisResult = value;
    }

}
