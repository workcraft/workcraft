package org.workcraft.commands;

import org.workcraft.util.DialogUtils;

public class CommandUtils {

    public static void commandRequiresGui(String commandName) {
        DialogUtils.showError("Command '" + commandName + "' requires GUI and cannot be scripted.");
    }

}
