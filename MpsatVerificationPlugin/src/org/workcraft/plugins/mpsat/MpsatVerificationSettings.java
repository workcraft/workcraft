package org.workcraft.plugins.mpsat;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.gui.properties.Settings;
import org.workcraft.plugins.mpsat.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.ConformationNwayOutputHandler.ConformationReportStyle;
import org.workcraft.utils.DesktopApi;

import java.util.LinkedList;
import java.util.List;

public class MpsatVerificationSettings implements Settings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatVerification";

    private static final String keyCommand = prefix + ".command";
    private static final String keySolutionMode = prefix + ".solutionMode";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyDebugReach = prefix + ".debugReach";
    private static final String keyDebugCores = prefix + ".debugCores";
    private static final String keyConformationReportStyle = prefix + ".conformationReportStyle";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\mpsat.exe" : "tools/UnfoldingTools/mpsat";
    private static final SolutionMode defaultSolutionMode = SolutionMode.MINIMUM_COST;
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultDebugReach = false;
    private static final Boolean defaultDebugCores = false;
    private static final ConformationReportStyle defaultConformationReportStyle = ConformationReportStyle.TABLE;

    private static String command = defaultCommand;
    private static SolutionMode solutionMode = defaultSolutionMode;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean debugReach = defaultDebugReach;
    private static Boolean debugCores = defaultDebugCores;
    private static ConformationReportStyle conformationReportStyle = defaultConformationReportStyle;

    public MpsatVerificationSettings() {
        properties.add(new PropertyDeclaration<MpsatVerificationSettings, String>(
                this, "MPSat command for verification", String.class) {
            @Override
            public void setter(MpsatVerificationSettings object, String value) {
                setCommand(value);
            }
            @Override
            public String getter(MpsatVerificationSettings object) {
                return getCommand();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, SolutionMode>(
                this, "Solution mode", SolutionMode.class) {
            @Override
            public void setter(MpsatVerificationSettings object, SolutionMode value) {
                setSolutionMode(value);
            }
            @Override
            public SolutionMode getter(MpsatVerificationSettings object) {
                return getSolutionMode();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, String>(
                this, "Additional parameters", String.class) {
            @Override
            public void setter(MpsatVerificationSettings object, String value) {
                setArgs(value);
            }
            @Override
            public String getter(MpsatVerificationSettings object) {
                return getArgs();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, Boolean>(
                this, "Edit additional parameters before every call", Boolean.class) {
            @Override
            public void setter(MpsatVerificationSettings object, Boolean value) {
                setAdvancedMode(value);
            }
            @Override
            public Boolean getter(MpsatVerificationSettings object) {
                return getAdvancedMode();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, Boolean>(
                this, "Output stdout", Boolean.class) {
            @Override
            public void setter(MpsatVerificationSettings object, Boolean value) {
                setPrintStdout(value);
            }
            @Override
            public Boolean getter(MpsatVerificationSettings object) {
                return getPrintStdout();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, Boolean>(
                this, "Output stderr", Boolean.class) {
            @Override
            public void setter(MpsatVerificationSettings object, Boolean value) {
                setPrintStderr(value);
            }
            @Override
            public Boolean getter(MpsatVerificationSettings object) {
                return getPrintStderr();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, Boolean>(
                this, "Output Reach expressions", Boolean.class) {
            @Override
            public void setter(MpsatVerificationSettings object, Boolean value) {
                setDebugReach(value);
            }
            @Override
            public Boolean getter(MpsatVerificationSettings object) {
                return getDebugReach();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, Boolean>(
                this, "Output conflict cores", Boolean.class) {
            @Override
            public void setter(MpsatVerificationSettings object, Boolean value) {
                setDebugCores(value);
            }
            @Override
            public Boolean getter(MpsatVerificationSettings object) {
                return getDebugCores();
            }
        });

        properties.add(new PropertyDeclaration<MpsatVerificationSettings, ConformationReportStyle>(
                this, "Report style for conformation violation", ConformationReportStyle.class) {
            @Override
            public void setter(MpsatVerificationSettings object, ConformationReportStyle value) {
                setConformationReportStyle(value);
            }
            @Override
            public ConformationReportStyle getter(MpsatVerificationSettings object) {
                return getConformationReportStyle();
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
        setSolutionMode(config.getEnum(keySolutionMode, SolutionMode.class, defaultSolutionMode));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setDebugReach(config.getBoolean(keyDebugReach, defaultDebugReach));
        setDebugCores(config.getBoolean(keyDebugCores, defaultDebugCores));
        setConformationReportStyle(config.getEnum(keyConformationReportStyle, ConformationReportStyle.class, defaultConformationReportStyle));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.setEnum(keySolutionMode, getSolutionMode());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyDebugReach, getDebugReach());
        config.setBoolean(keyDebugCores, getDebugCores());
        config.setEnum(keyConformationReportStyle, getConformationReportStyle());
    }

    @Override
    public String getSection() {
        return "External tools";
    }

    @Override
    public String getName() {
        return "MPSat verification";
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

    public static void setSolutionMode(SolutionMode value) {
        solutionMode = value;
    }

    public static SolutionMode getSolutionMode() {
        return solutionMode;
    }

    public static int getSolutionCount() {
        return (solutionMode == SolutionMode.ALL) ? 10 : 1;
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

    public static Boolean getDebugReach() {
        return debugReach;
    }

    public static void setDebugReach(Boolean value) {
        debugReach = value;
    }

    public static Boolean getDebugCores() {
        return debugCores;
    }

    public static void setDebugCores(Boolean value) {
        debugCores = value;
    }

    public static void setConformationReportStyle(ConformationReportStyle value) {
        conformationReportStyle = value;
    }

    public static ConformationReportStyle getConformationReportStyle() {
        return conformationReportStyle;
    }

}
