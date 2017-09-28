package org.workcraft.plugins.mpsat.commands;

import java.io.File;

import javax.swing.JFileChooser;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatUtils;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResult;
import org.workcraft.plugins.mpsat.tasks.MpsatConformationTask;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatConformationVerificationCommand extends AbstractVerificationCommand {

    private File envFile;

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

    public void setEnvironment(File envFile) {
        this.envFile = envFile;
    }

    public File getEnvironment() {
        return envFile;
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (check(stg) && FileUtils.checkAvailability(getEnvironment(), null)) {
            Stg envStg = StgUtils.loadStg(getEnvironment());
            if (envStg == null) {
                DialogUtils.showError("Cannot read an STG model from the file:\n"
                        + getEnvironment().getAbsolutePath() + "\n\n"
                        + "Conformation cannot be checked without environment STG.");
                return null;
            }
            Framework framework = Framework.getInstance();
            TaskManager manager = framework.getTaskManager();
            MpsatConformationTask task = new MpsatConformationTask(we, getEnvironment());
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            Result<? extends MpsatChainResult> result = manager.execute(task, description);
            return MpsatUtils.getChainOutcome(result);
        }
        return null;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (check(stg)) {
            Importer[] importers = {new StgImporter()};
            JFileChooser fc = mainWindow.createOpenDialog("Open environment file", false, true, importers);
            if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
                setEnvironment(fc.getSelectedFile());
                if (FileUtils.checkAvailability(getEnvironment(), null)) {
                    Stg envStg = StgUtils.loadStg(getEnvironment());
                    if (envStg == null) {
                        DialogUtils.showError("Cannot read an STG model from the file:\n"
                                + getEnvironment().getAbsolutePath() + "\n\n"
                                + "Conformation cannot be checked without environment STG.");
                        return;
                    }
                    TaskManager manager = framework.getTaskManager();
                    MpsatConformationTask task = new MpsatConformationTask(we, getEnvironment());
                    String description = MpsatUtils.getToolchainDescription(we.getTitle());
                    MpsatChainResultHandler monitor = new MpsatChainResultHandler(task);
                    manager.queue(task, description, monitor);
                }
            }
        }
    }

    private boolean check(Stg stg) {
        // Check for limitations:
        if (stg.getPlaces().isEmpty()) {
            // - The set of device STG place names is non-empty (this limitation can be easily removed).
            DialogUtils.showError("For conformation chech the STG must have places.");
            return false;
        }

        if (hasDisconnectedTransitions(stg)) {
            // - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.
            DialogUtils.showError("For conformation chech the STG must have no disconnected transitions.");
            return false;
        }

        if (!stg.getDummyTransitions().isEmpty()) {
            // - The device STG must have no dummies.
            DialogUtils.showError("For conformation chech the STG must have no dummies.");
            return false;
        }
        return true;
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
