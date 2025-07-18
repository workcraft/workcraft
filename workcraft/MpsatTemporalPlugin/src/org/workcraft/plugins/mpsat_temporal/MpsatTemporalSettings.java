package org.workcraft.plugins.mpsat_temporal;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class MpsatTemporalSettings extends AbstractToolSettings {

    public static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    public static final String prefix = "Tools.mpsatTemporal";

    private static final String keyCommand = prefix + ".command";
    private static final String keyThreadCount = prefix + ".threadCount";
    private static final String keyReplicateSelfloopPlaces = prefix + ".replicateSelfloopPlaces";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyLtl2tgbaCommand = prefix + ".ltl2tgbaCommand";
    private static final String keyShowSpotInMenu = prefix + ".showSpotInMenu";

    private static final String defaultCommand = BackendUtils.getToolPath("UnfoldingTools", "mpsat");
    private static final int defaultThreadCount = 8;
    private static final boolean defaultReplicateSelfloopPlaces = false;
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final String defaultLtl2tgbaCommand = BackendUtils.getToolPath("Spot", "ltl2tgba");
    private static final Boolean defaultShowSpotInMenu = false;

    private static String command = defaultCommand;
    private static int threadCount = defaultThreadCount;
    private static boolean replicateSelfloopPlaces = defaultReplicateSelfloopPlaces;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static String ltl2tgbaCommand = defaultLtl2tgbaCommand;
    private static Boolean showSpotInMenu = defaultShowSpotInMenu;

    static {
        PropertyDeclaration<String> commandProperty = new PropertyDeclaration<>(String.class,
                "MPSat command for temporal verification",
                MpsatTemporalSettings::setCommand,
                MpsatTemporalSettings::getCommand);

        properties.add(commandProperty);

        properties.add(new PropertyDeclaration<>(Integer.class,
                "Number of threads for unfolding (8 by default, 0 for automatic)",
                MpsatTemporalSettings::setThreadCount,
                MpsatTemporalSettings::getThreadCount));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Replicate places with multiple self-loops for unfolding (-l parameter)",
                MpsatTemporalSettings::setReplicateSelfloopPlaces,
                MpsatTemporalSettings::getReplicateSelfloopPlaces));


        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                MpsatTemporalSettings::setArgs,
                MpsatTemporalSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Edit additional parameters before every call",
                MpsatTemporalSettings::setAdvancedMode,
                MpsatTemporalSettings::getAdvancedMode));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                MpsatTemporalSettings::setPrintStdout,
                MpsatTemporalSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                MpsatTemporalSettings::setPrintStderr,
                MpsatTemporalSettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(String.class,
                "Building B\u00FCchi automaton command",
                MpsatTemporalSettings::setLtl2tgbaCommand,
                MpsatTemporalSettings::getLtl2tgbaCommand));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Enable SPOT input (experimental)",
                MpsatTemporalSettings::setShowSpotInMenu,
                MpsatTemporalSettings::getShowSpotInMenu));
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
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setLtl2tgbaCommand(config.getString(keyLtl2tgbaCommand, defaultLtl2tgbaCommand));
        setShowSpotInMenu(config.getBoolean(keyShowSpotInMenu, defaultShowSpotInMenu));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.setInt(keyThreadCount, getThreadCount());
        config.setBoolean(keyReplicateSelfloopPlaces, getReplicateSelfloopPlaces());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.set(keyLtl2tgbaCommand, getLtl2tgbaCommand());
        config.setBoolean(keyShowSpotInMenu, getShowSpotInMenu());
    }

    @Override
    public String getName() {
        return "MPSat temporal";
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

    public static String getLtl2tgbaCommand() {
        return ltl2tgbaCommand;
    }

    public static void setLtl2tgbaCommand(String value) {
        ltl2tgbaCommand = value;
    }

    public static boolean getShowSpotInMenu() {
        return showSpotInMenu;
    }

    public static void setShowSpotInMenu(boolean value) {
        showSpotInMenu = value;
    }

}
