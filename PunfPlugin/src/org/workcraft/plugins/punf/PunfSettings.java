package org.workcraft.plugins.punf;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.DesktopApi;

import java.util.LinkedList;
import java.util.List;

public class PunfSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor> properties = new LinkedList<>();
    private static final String prefix = "Tools.punf";

    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";
    private static final String keyUseMciCsc = prefix + ".useMciCsc";

    private static final String defaultCommand = DesktopApi.getOs().isWindows() ? "tools\\UnfoldingTools\\punf.exe" : "tools/UnfoldingTools/punf";
    private static final String defaultArgs = "-r";
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;
    private static final Boolean defaultUseMciCsc = true;

    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;
    private static Boolean useMciCsc = defaultUseMciCsc;

    static {
        properties.add(new PropertyDeclaration<>(String.class,
                "Punf command",
                PunfSettings::setCommand,
                PunfSettings::getCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                PunfSettings::setArgs,
                PunfSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                PunfSettings::setPrintStdout,
                PunfSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                PunfSettings::setPrintStderr,
                PunfSettings::getPrintStderr));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Use legacy MCI unfolding for CSC conflict resolution",
                PunfSettings::setUseMciCsc,
                PunfSettings::getUseMciCsc));
    }

    @Override
    public List<PropertyDescriptor> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
        setUseMciCsc(config.getBoolean(keyUseMciCsc, defaultUseMciCsc));
    }

    @Override
    public void save(Config config) {
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
        config.setBoolean(keyUseMciCsc, getUseMciCsc());
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

    public static Boolean getUseMciCsc() {
        return useMciCsc;
    }

    public static void setUseMciCsc(Boolean value) {
        useMciCsc = value;
    }

}
