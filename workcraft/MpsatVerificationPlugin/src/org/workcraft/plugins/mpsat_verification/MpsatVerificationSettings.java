package org.workcraft.plugins.mpsat_verification;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat_verification.tasks.CompositionOutputInterpreter.ConformationReportStyle;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class MpsatVerificationSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatVerification";

    private static final String keyCommand = prefix + ".command";
    private static final String keyThreadCount = prefix + ".threadCount";
    private static final String keyReplicateSelfloopPlaces = prefix + ".replicateSelfloopPlaces";
    private static final String keySolutionMode = prefix + ".solutionMode";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyDebugReach = prefix + ".debugReach";
    private static final String keyDebugCores = prefix + ".debugCores";
    private static final String keyConformationReportStyle = prefix + ".conformationReportStyle";

    private static final String defaultCommand = BackendUtils.getToolPath("UnfoldingTools", "mpsat");
    private static final int defaultThreadCount = 8;
    private static final boolean defaultReplicateSelfloopPlaces = true;
    private static final SolutionMode defaultSolutionMode = SolutionMode.MINIMUM_COST;
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultDebugReach = false;
    private static final Boolean defaultDebugCores = false;
    private static final ConformationReportStyle defaultConformationReportStyle = ConformationReportStyle.TABLE;

    private static String command = defaultCommand;
    private static int threadCount = defaultThreadCount;
    private static boolean replicateSelfloopPlaces = defaultReplicateSelfloopPlaces;
    private static SolutionMode solutionMode = defaultSolutionMode;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean debugReach = defaultDebugReach;
    private static Boolean debugCores = defaultDebugCores;
    private static ConformationReportStyle conformationReportStyle = defaultConformationReportStyle;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "MPSat command for verification",
                MpsatVerificationSettings::setCommand,
                MpsatVerificationSettings::getCommand));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Number of threads for unfolding (8 by default, 0 for automatic)",
                MpsatVerificationSettings::setThreadCount,
                MpsatVerificationSettings::getThreadCount));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Replicate places with multiple self-loops for unfolding (-l parameter)",
                MpsatVerificationSettings::setReplicateSelfloopPlaces,
                MpsatVerificationSettings::getReplicateSelfloopPlaces));

        properties.add(new PropertyDeclaration<>(SolutionMode.class,
                "Solution mode",
                MpsatVerificationSettings::setSolutionMode,
                MpsatVerificationSettings::getSolutionMode));

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                MpsatVerificationSettings::setArgs,
                MpsatVerificationSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Edit additional parameters before every call",
                MpsatVerificationSettings::setAdvancedMode,
                MpsatVerificationSettings::getAdvancedMode));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                MpsatVerificationSettings::setPrintStdout,
                MpsatVerificationSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                MpsatVerificationSettings::setPrintStderr,
                MpsatVerificationSettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output REACH expressions",
                MpsatVerificationSettings::setDebugReach,
                MpsatVerificationSettings::getDebugReach));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output conflict cores",
                MpsatVerificationSettings::setDebugCores,
                MpsatVerificationSettings::getDebugCores));

        properties.add(new PropertyDeclaration<>(ConformationReportStyle.class,
                "Report style for conformation violation",
                MpsatVerificationSettings::setConformationReportStyle,
                MpsatVerificationSettings::getConformationReportStyle));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setThreadCount(config.getInt(keyThreadCount, defaultThreadCount));
        setReplicateSelfloopPlaces(config.getBoolean(keyReplicateSelfloopPlaces, defaultReplicateSelfloopPlaces));
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
        config.setInt(keyThreadCount, getThreadCount());
        config.setBoolean(keyReplicateSelfloopPlaces, getReplicateSelfloopPlaces());
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
    public String getName() {
        return "MPSat verification";
    }

    public static String getCommand() {
        return command;
    }

    public static void setCommand(String value) {
        command = value;
    }

    public static int getThreadCount() {
        return threadCount;
    }

    public static void setThreadCount(int value) {
        if (value >= 0) {
            threadCount = value;
        }
    }

    public static boolean getReplicateSelfloopPlaces() {
        return replicateSelfloopPlaces;
    }

    public static void setReplicateSelfloopPlaces(boolean value) {
        replicateSelfloopPlaces = value;
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
