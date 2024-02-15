package org.workcraft.plugins.mpsat_synthesis;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MpsatSynthesisSettings extends AbstractToolSettings {

    public static final Map<String, String> COST_FUNCTION_CONFLICT_RESOLUTION = new LinkedHashMap<>();
    public static final Map<String, String> COST_FUNCTION_TECHNOLOGY_MAPPING = new LinkedHashMap<>();

    static {
        COST_FUNCTION_CONFLICT_RESOLUTION.put("", "Use default coefficients");
        COST_FUNCTION_CONFLICT_RESOLUTION.put(
                "-fconc=#s*10 -fcsc=#s*10 -fcomp_csc=#s*1 -fusc=#s*5 -fcomp_usc=#s*0 -ftrig=#s*4 -fseq=#s*1 -fseq_inc=#s*4 -flock=51",
                "");

        COST_FUNCTION_TECHNOLOGY_MAPPING.put("", "Use default coefficients");
        COST_FUNCTION_TECHNOLOGY_MAPPING.put(
                "-fconc=#s*10 -fcsc=#s*0 -fcomp_csc=#s*0 -fusc=#s*0 -fcomp_usc=#s*0 -ftrig=#s*40 -fseq=#s*1 -fseq_inc=#s*2 -flock=16",
                "");
    }

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatSynthesis";

    private static final String keyCommand = prefix + ".command";
    private static final String keyThreadCount = prefix + ".threadCount";
    private static final String keyReplicateSelfloopPlaces = prefix + ".replicateSelfloopPlaces";
    private static final String keyCostFunctionConflictResolution = prefix + ".costFunctionConflictResolution";
    private static final String keyCostFunctionTechnologyMapping = prefix + ".costFunctionTechnologyMapping";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyOpenSynthesisStg = prefix + ".openSynthesisStg";

    private static final String defaultCommand = BackendUtils.getToolPath("UnfoldingTools", "mpsat");
    private static final int defaultThreadCount = 8;
    private static final boolean defaultReplicateSelfloopPlaces = true;
    private static final String defaultCostFunctionConflictResolution = "";
    private static final String defaultCostFunctionTechnologyMapping = "";
    private static final String defaultArgs = "";
    private static final boolean defaultAdvancedMode = false;
    private static final boolean defaultPrintStdout = true;
    private static final boolean defaultPrintStderr = true;
    private static final boolean defaultOpenSynthesisStg = false;

    private static String command = defaultCommand;
    private static int threadCount = defaultThreadCount;
    private static boolean replicateSelfloopPlaces = defaultReplicateSelfloopPlaces;
    private static String costFunctionConflictResolution = defaultCostFunctionConflictResolution;
    private static String costFunctionTechnologyMapping = defaultCostFunctionTechnologyMapping;
    private static String args = defaultArgs;
    private static boolean advancedMode = defaultAdvancedMode;
    private static boolean printStdout = defaultPrintStdout;
    private static boolean printStderr = defaultPrintStderr;
    private static boolean openSynthesisStg = defaultOpenSynthesisStg;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "MPSat command for synthesis",
                MpsatSynthesisSettings::setCommand,
                MpsatSynthesisSettings::getCommand));

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Number of threads for unfolding (" + defaultThreadCount + " by default, 0 for automatic)",
                MpsatSynthesisSettings::setThreadCount,
                MpsatSynthesisSettings::getThreadCount));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Replicate places with multiple self-loops for unfolding (-l parameter)",
                MpsatSynthesisSettings::setReplicateSelfloopPlaces,
                MpsatSynthesisSettings::getReplicateSelfloopPlaces));

        properties.add(new PropertyDeclaration<>(String.class,
                "Cost function coefficients for CSC conflict resolution",
                MpsatSynthesisSettings::setCostFunctionConflictResolution,
                MpsatSynthesisSettings::getCostFunctionConflictResolution) {
            @Override
            public Map<String, String> getChoice() {
                return COST_FUNCTION_CONFLICT_RESOLUTION;
            }
        });

        properties.add(new PropertyDeclaration<>(String.class,
                "Cost function coefficients for technology mapping",
                MpsatSynthesisSettings::setCostFunctionTechnologyMapping,
                MpsatSynthesisSettings::getCostFunctionTechnologyMapping) {
            @Override
            public Map<String, String> getChoice() {
                return COST_FUNCTION_TECHNOLOGY_MAPPING;
            }
        });

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                MpsatSynthesisSettings::setArgs,
                MpsatSynthesisSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Edit additional parameters before every call",
                MpsatSynthesisSettings::setAdvancedMode,
                MpsatSynthesisSettings::getAdvancedMode));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                MpsatSynthesisSettings::setPrintStdout,
                MpsatSynthesisSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                MpsatSynthesisSettings::setPrintStderr,
                MpsatSynthesisSettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Open resulting STG if new signals are inserted",
                MpsatSynthesisSettings::setOpenSynthesisStg,
                MpsatSynthesisSettings::getOpenSynthesisStg));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setThreadCount(config.getInt(keyThreadCount, defaultThreadCount));
        setReplicateSelfloopPlaces(config.getBoolean(keyReplicateSelfloopPlaces, defaultReplicateSelfloopPlaces));
        setCostFunctionConflictResolution(config.getString(keyCostFunctionConflictResolution, defaultCostFunctionConflictResolution));
        setCostFunctionTechnologyMapping(config.getString(keyCostFunctionTechnologyMapping, defaultCostFunctionTechnologyMapping));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setOpenSynthesisStg(config.getBoolean(keyOpenSynthesisStg, defaultOpenSynthesisStg));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.setInt(keyThreadCount, getThreadCount());
        config.setBoolean(keyReplicateSelfloopPlaces, getReplicateSelfloopPlaces());
        config.set(keyCostFunctionConflictResolution, getCostFunctionConflictResolution());
        config.set(keyCostFunctionTechnologyMapping, getCostFunctionTechnologyMapping());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyOpenSynthesisStg, getOpenSynthesisStg());
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

    public static String getCostFunctionConflictResolution() {
        return costFunctionConflictResolution;
    }

    public static void setCostFunctionConflictResolution(String value) {
        costFunctionConflictResolution = value;
    }

    public static String getCostFunctionTechnologyMapping() {
        return costFunctionTechnologyMapping;
    }

    public static void setCostFunctionTechnologyMapping(String value) {
        costFunctionTechnologyMapping = value;
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

    public static boolean getOpenSynthesisStg() {
        return openSynthesisStg;
    }

    public static void setOpenSynthesisStg(boolean value) {
        openSynthesisStg = value;
    }

}
