package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.plugins.PluginManager;

public class ScriptableCommandUtils {

    public static void showErrorRequiresGui(Class<? extends Command> commandClass) {
        String commandName = commandClass.getSimpleName();
        DialogUtils.showError("Command '" + commandName + "' requires GUI and cannot be scripted.");
    }

    public static void register(Class<? extends ScriptableCommand> commandClass, String jsName) {
        String name = commandClass.getName();
        String help = "wrapper for framework.executeCommand(work, '" + name + "')";
        register(commandClass, jsName, help);
    }

    public static void register(Class<? extends ScriptableCommand> commandClass, String jsName, String jsHelp) {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerCommand(commandClass);
        String name = commandClass.getName();
        framework.registerJavaScriptFunction(
                "function " + jsName + "(work) {\n" +
                        "    return framework.executeCommand(work, '" + name + "');\n" +
                        "}", jsHelp);
    }

    public static void registerDataCommand(Class<? extends ScriptableDataCommand> commandClass,
            String jsName, String jsHelp) {

        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerCommand(commandClass);
        String name = commandClass.getName();
        framework.registerJavaScriptFunction(
                "function " + jsName + "(work, data) {\n" +
                        "    return framework.executeCommand(work, '" + name + "', data);\n" +
                        "}", jsHelp);
    }

}
