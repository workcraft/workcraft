package org.workcraft.plugins.atacs;

import org.workcraft.Config;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.properties.PropertyDescriptor;
import org.workcraft.plugins.builtin.settings.AbstractToolSettings;
import org.workcraft.utils.BackendUtils;

import java.util.LinkedList;
import java.util.List;

public class AtacsSettings extends AbstractToolSettings {

    private static final LinkedList<PropertyDescriptor<?>> properties = new LinkedList<>();
    private static final String prefix = "Tools.atacs";

    private static final String keyShowInMenu = prefix + ".showInMenu";
    private static final String keyCommand = prefix + ".command";
    private static final String keyArgs = prefix + ".args";
    private static final String keyAdvancedMode = prefix + ".advancedMode";
    private static final String keyPrintStdout = prefix + ".printStdout";
    private static final String keyPrintStderr = prefix + ".printStderr";

    private static final Boolean defaultShowInMenu = false;
    private static final String defaultCommand = BackendUtils.getToolPath("ATACS", "atacs");
    private static final String defaultArgs = "";
    private static final Boolean defaultAdvancedMode = false;
    private static final Boolean defaultPrintStdout = true;
    private static final Boolean defaultPrintStderr = true;

    private static Boolean showInMenu = defaultShowInMenu;
    private static String command = defaultCommand;
    private static String args = defaultArgs;
    private static Boolean advancedMode = defaultAdvancedMode;
    private static Boolean printStdout = defaultPrintStdout;
    private static Boolean printStderr = defaultPrintStderr;

    static {
        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Activate ATACS synthesis (experimental)",
                AtacsSettings::setShowInMenu,
                AtacsSettings::getShowInMenu));

        properties.add(new PropertyDeclaration<>(String.class,
                "ATACS command",
                AtacsSettings::setCommand,
                AtacsSettings::getCommand));

        properties.add(new PropertyDeclaration<>(String.class,
                "Additional parameters",
                AtacsSettings::setArgs,
                AtacsSettings::getArgs));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Edit additional parameters before every call",
                AtacsSettings::setAdvancedMode,
                AtacsSettings::getAdvancedMode));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stdout",
                AtacsSettings::setPrintStdout,
                AtacsSettings::getPrintStdout));

        properties.add(new PropertyDeclaration<>(Boolean.class,
                "Output stderr",
                AtacsSettings::setPrintStderr,
                AtacsSettings::getPrintStderr));
    }

    @Override
    public List<PropertyDescriptor<?>> getDescriptors() {
        return properties;
    }

    @Override
    public void load(Config config) {
        setShowInMenu(config.getBoolean(keyShowInMenu, defaultShowInMenu));
        setCommand(config.getString(keyCommand, defaultCommand));
        setArgs(config.getString(keyArgs, defaultArgs));
        setAdvancedMode(config.getBoolean(keyAdvancedMode, defaultAdvancedMode));
        setPrintStdout(config.getBoolean(keyPrintStdout, defaultPrintStdout));
        setPrintStderr(config.getBoolean(keyPrintStderr, defaultPrintStderr));
    }

    @Override
    public void save(Config config) {
        config.setBoolean(keyShowInMenu, getShowInMenu());
        config.set(keyCommand, getCommand());
        config.set(keyArgs, getArgs());
        config.setBoolean(keyAdvancedMode, getAdvancedMode());
        config.setBoolean(keyPrintStdout, getPrintStdout());
        config.setBoolean(keyPrintStderr, getPrintStderr());
    }

    @Override
    public String getName() {
        return "ATACS";
    }

    public static boolean getShowInMenu() {
        return showInMenu;
    }

    public static void setShowInMenu(boolean value) {
        showInMenu = value;
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

}
