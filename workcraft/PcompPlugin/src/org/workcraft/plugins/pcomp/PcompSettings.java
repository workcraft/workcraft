package org.workcraft.plugins.pcomp;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.plugins.pcomp.tasks.PcompParameters;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class PcompSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "Tools.pcomp";

    private static final String ketCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keySharedSignalMode = prefix + ".sharedSignalMode";

    private static final String defaultCommand = BackendUtils.getToolPath("UnfoldingTools", "pcomp");
    private static final String defaultArgs = "";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final PcompParameters.SharedSignalMode defaultSharedSignalMode = PcompParameters.SharedSignalMode.OUTPUT;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static PcompParameters.SharedSignalMode sharedSignalMode = defaultSharedSignalMode;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "PComp command",
                PcompSettings::setCommand,
                PcompSettings::getCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                PcompSettings::setArgs,
                PcompSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                PcompSettings::setPrintStdout,
                PcompSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                PcompSettings::setPrintStderr,
                PcompSettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(PcompParameters.SharedSignalMode.class,
                "Conversion of shared signals (leave as outputs by default)",
                PcompSettings::setSharedSignalMode,
                PcompSettings::getSharedSignalMode));

    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(ketCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setSharedSignalMode(config.getEnum(keySharedSignalMode, PcompParameters.SharedSignalMode.class, defaultSharedSignalMode));
    }

    @Override
    public void save(Config config) {
        config.set(ketCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setEnum(keySharedSignalMode, getSharedSignalMode());
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

    public static void setSharedSignalMode(PcompParameters.SharedSignalMode value) {
        sharedSignalMode = value;
    }

    public static PcompParameters.SharedSignalMode getSharedSignalMode() {
        return sharedSignalMode;
    }

}
