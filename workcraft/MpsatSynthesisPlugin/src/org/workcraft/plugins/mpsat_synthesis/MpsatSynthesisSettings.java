package org.workcraft.plugins.mpsat_synthesis;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class MpsatSynthesisSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.mpsatSynthesis";

    private static final String keyCommand = prefix + ".command";
    private static final String keyReplicateSelfloopPlaces = prefix + ".replicateSelfloopPlaces";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyOpenSynthesisStg = prefix + ".openSynthesisStg";

    private static final String defaultCommand = BackendUtils.getToolPath("UnfoldingTools", "mpsat");
    private static final boolean defaultReplicateSelfloopPlaces = true;
    private static final String defaultArgs = "";
    private static final boolean defaultAdvancedMode = false;
    private static final boolean defaultPrintStdout = true;
    private static final boolean defaultPrintStderr = true;
    private static final boolean defaultOpenSynthesisStg = false;

    private static String command = defaultCommand;
    private static boolean replicateSelfloopPlaces = defaultReplicateSelfloopPlaces;
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

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Replicate places with multiple self-loops (-l parameter)",
                MpsatSynthesisSettings::setReplicateSelfloopPlaces,
                MpsatSynthesisSettings::getReplicateSelfloopPlaces));

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
        setReplicateSelfloopPlaces(config.getBoolean(keyReplicateSelfloopPlaces, defaultReplicateSelfloopPlaces));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setOpenSynthesisStg(config.getBoolean(keyOpenSynthesisStg, defaultOpenSynthesisStg));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.setBoolean(keyReplicateSelfloopPlaces, getReplicateSelfloopPlaces());
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

    public static boolean getReplicateSelfloopPlaces() {
        return replicateSelfloopPlaces;
    }

    public static void setReplicateSelfloopPlaces(boolean value) {
        replicateSelfloopPlaces = value;
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
