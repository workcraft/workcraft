package org.workcraft.plugins.mpsat.commands;

import java.io.File;

import javax.swing.JFileChooser;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.CommandUtils;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatConformationTask;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatConformationVerificationCommand extends AbstractVerificationCommand {

    @Override
    public String getDisplayName() {
        return "   Conformation (without dummies) [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
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
    public final void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        // Check for limitations:
        if (stg.getPlaces().isEmpty()) {
            // - The set of device STG place names is non-empty (this limitation can be easily removed).
            DialogUtils.showError("For conformation chech the STG must have places.");
            return;
        }

        if (hasDisconnectedTransitions(stg)) {
            // - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.
            DialogUtils.showError("For conformation chech the STG must have no disconnected transitions.");
            return;
        }

        if (!stg.getDummyTransitions().isEmpty()) {
            // - The device STG must have no dummies.
            DialogUtils.showError("For conformation chech the STG must have no dummies.");
            return;
        }

        Importer[] importers = {new StgImporter()};
        JFileChooser fc = mainWindow.createOpenDialog("Open environment file", false, true, importers);
        if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
            File envFile = fc.getSelectedFile();
            if (mainWindow.checkFileMessageDialog(envFile, null)) {
                Stg envStg = StgUtils.loadStg(envFile);
                if (envStg == null) {
                    DialogUtils.showError("Cannot read an STG model from the file:\n"
                            + envFile.getAbsolutePath() + "\n\n"
                            + "Conformation cannot be checked without environment STG.");
                } else {
                    TaskManager manager = framework.getTaskManager();
                    MpsatConformationTask task = new MpsatConformationTask(we, envFile);
                    String description = MpsatUtils.getToolchainDescription(we.getTitle());
                    MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
                    manager.queue(task, description, monitor);
                }
            }
        }
    }

    private boolean hasDisconnectedTransitions(Stg stg) {
        for (Transition t: stg.getTransitions()) {
            if (stg.getPreset(t).isEmpty() && stg.getPostset(t).isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
