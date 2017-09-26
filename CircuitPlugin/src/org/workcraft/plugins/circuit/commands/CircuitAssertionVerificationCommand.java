package org.workcraft.plugins.circuit.commands;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.CommandUtils;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.tasks.CustomCheckCircuitTask;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.commands.MpsatAssertionVerificationCommand;
import org.workcraft.plugins.mpsat.gui.MpsatAssertionDialog;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        CommandUtils.commandRequiresGui(getClass().getSimpleName());
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();

        Circuit circuit = WorkspaceUtils.getAs(we, Circuit.class);
        if (circuit.getFunctionComponents().isEmpty()) {
            DialogUtils.showError("The circuit must have components.");
        } else {
            VisualCircuit visualCircuit = WorkspaceUtils.getAs(we, VisualCircuit.class);
            File envFile = visualCircuit.getEnvironmentFile();
            Stg envStg = StgUtils.loadStg(envFile);
            if (envStg == null) {
                String messagePrefix = "";
                if (envFile != null) {
                    messagePrefix = "Cannot read an STG model from the file:\n" + envFile.getAbsolutePath() + "\n\n";
                }
                DialogUtils.showWarning(messagePrefix + "The circuit will be verified without environment STG.");
            }
            File presetFile = new File(Framework.SETTINGS_DIRECTORY_PATH, MpsatAssertionVerificationCommand.MPSAT_ASSERTION_PRESETS_FILE);
            MpsatPresetManager pmgr = new MpsatPresetManager(presetFile, new MpsatSettingsSerialiser(), true);
            MpsatAssertionDialog dialog = new MpsatAssertionDialog(mainWindow, pmgr);
            dialog.pack();
            GUI.centerToParent(dialog, mainWindow);
            dialog.setVisible(true);
            if (dialog.getModalResult() == 1) {
                TaskManager manager = framework.getTaskManager();
                CustomCheckCircuitTask task = new CustomCheckCircuitTask(we, dialog.getSettings());
                String description = MpsatUtils.getToolchainDescription(we.getTitle());
                MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
                manager.queue(task, description, monitor);
            }
        }
    }

}
