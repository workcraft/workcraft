package org.workcraft.utils;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.plugins.PluginManager;

public class ScriptableCommandUtils {

    public static void showErrorRequiresGui(String commandName) {
        DialogUtils.showError("Command '" + commandName + "' requires GUI and cannot be scripted.");
    }

    public static void register(Class<? extends ScriptableCommand> command, String jsName) {
        String name = command.getName();
        String help = "wrapper for framework.executeCommand(work, '" + name + "')";
        register(command, jsName, help);
    }

    public static void register(Class<? extends ScriptableCommand> command, String jsName, String jsHelp) {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerCommand(command);
        String name = command.getName();
        framework.registerJavaScriptFunction(
                "function " + jsName + "(work) {\n" +
                "    return framework.executeCommand(work, '" + name + "');\n" +
                "}", jsHelp);
    }

}
