package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.Command;

public class CommandAction extends Action {
    Command command;

    public CommandAction(Command command) {
        this.command = command;
    }

    public String getText() {
        return command.getDisplayName().trim();
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        mainWindow.runCommand(command);
    }

}
