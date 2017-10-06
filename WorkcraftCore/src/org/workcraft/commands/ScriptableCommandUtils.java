package org.workcraft.commands;

import org.workcraft.Framework;
import org.workcraft.PluginManager;
import org.workcraft.util.DialogUtils;

public class ScriptableCommandUtils {

    public static void showErrorRequiresGui(String commandName) {
        DialogUtils.showError("Command '" + commandName + "' requires GUI and cannot be scripted.");
    }

    public static void register(Class<? extends ScriptableCommand> command) {
        register(command, command.getSimpleName());
    }

    public static void register(Class<? extends ScriptableCommand> command, String jsName) {
        String help = "wrapper for framework.executeCommand(work, '" + jsName + "')";
        register(command, jsName, help);
    }

    public static void register(Class<? extends ScriptableCommand> command, String jsName, String jsHelp) {
        final Framework framework = Framework.getInstance();
        PluginManager pm = framework.getPluginManager();
        pm.registerClass(Command.class, command);
        String commandName = command.getSimpleName();
        framework.registerJavaScriptFunction(
                "function " + jsName + "(work) {\n" +
                "    return framework.executeCommand(work, '" + commandName + "');\n" +
                "}", jsHelp);
    }

}
