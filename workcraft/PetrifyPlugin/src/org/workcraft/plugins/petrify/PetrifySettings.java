package org.workcraft.plugins.petrify;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class PetrifySettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "Tools.petrify";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyWriteLog = prefix + ".writeLog";
    private static final String keyWriteEqn = prefix + ".writeEqn";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyOpenSynthesisStg = prefix + ".openSynthesisStg";

    private static final String defaultCommand = BackendUtils.getToolPath("PetrifyTools", "petrify");
    private static final String defaultArgs = "";
    private static final boolean defaultAdvancedMode = false;
    private static final boolean defaultWriteLog = true;
    private static final boolean defaultWriteEqn = false;
    private static final boolean defaultPrintStdout = true;
    private static final boolean defaultPrintStderr = true;
    private static final boolean defaultOpenSynthesisStg = false;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static boolean advancedMode = defaultAdvancedMode;
    private static boolean writeLog = defaultWriteLog;
    private static boolean writeEqn = defaultWriteEqn;
    private static boolean printStdout = defaultPrintStdout;
    private static boolean printStderr = defaultPrintStderr;
    private static boolean openSynthesisStg = defaultOpenSynthesisStg;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "Petrify command",
                PetrifySettings::setCommand,
                PetrifySettings::getCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                PetrifySettings::setArgs,
                PetrifySettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Edit additional parameters before every call",
                PetrifySettings::setAdvancedMode,
                PetrifySettings::getAdvancedMode));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output detailed log (for circuit synthesis)",
                PetrifySettings::setWriteLog,
                PetrifySettings::getWriteLog));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output signal equations (for circuit synthesis)",
                PetrifySettings::setWriteEqn,
                PetrifySettings::getWriteEqn));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                PetrifySettings::setPrintStdout,
                PetrifySettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                PetrifySettings::setPrintStderr,
                PetrifySettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Open resulting STG if new signals are inserted",
                PetrifySettings::setOpenSynthesisStg,
                PetrifySettings::getOpenSynthesisStg));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setWriteLog(config.getBoolean(keyWriteLog, defaultWriteLog));
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
        config.setBoolean(keyWriteEqn, getWriteEqn());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyOpenSynthesisStg, getOpenSynthesisStg());
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
