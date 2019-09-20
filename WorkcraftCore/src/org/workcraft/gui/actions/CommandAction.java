package org.workcraft.gui.actions;

import org.workcraft.Framework;
import org.workcraft.commands.Command;
import org.workcraft.gui.MainWindow;

public class CommandAction extends Action {

    private final Command command;

    public CommandAction(Command command) {
        this.command = command;
    }

    @Override
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
