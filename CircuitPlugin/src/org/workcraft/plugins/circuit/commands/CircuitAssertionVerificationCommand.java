package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommandUtils;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.CircuitCustomCheckTask;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.commands.MpsatAssertionVerificationCommand;
import org.workcraft.plugins.mpsat.gui.MpsatAssertionDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import java.io.File;

public class CircuitAssertionVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "Custom assertion [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        ScriptableCommandUtils.showErrorRequiresGui(getClass().getSimpleName());
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return;
        }
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        File presetFile = new File(Framework.SETTINGS_DIRECTORY_PATH, MpsatAssertionVerificationCommand.MPSAT_ASSERTION_PRESETS_FILE);
        MpsatPresetManager pmgr = new MpsatPresetManager(presetFile, new MpsatSettingsSerialiser(), true);
        MpsatAssertionDialog dialog = new MpsatAssertionDialog(mainWindow, pmgr);
        dialog.pack();
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            TaskManager manager = framework.getTaskManager();
            CircuitCustomCheckTask task = new CircuitCustomCheckTask(we, dialog.getSettings());
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
            manager.queue(task, description, monitor);
        }
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return CircuitVerificationUtils.checkCircuitHasComponents(we)
            && CircuitVerificationUtils.checkInterfaceInitialState(we)
            && CircuitVerificationUtils.checkInterfaceConstrains(we);
    }

}