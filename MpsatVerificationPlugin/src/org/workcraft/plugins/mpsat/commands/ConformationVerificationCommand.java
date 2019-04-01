package org.workcraft.plugins.mpsat.commands;

import java.io.File;

import javax.swing.JFileChooser;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Importer;
import org.workcraft.plugins.mpsat.tasks.VerificationChainOutput;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.ConformationTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ConformationVerificationCommand extends AbstractVerificationCommand {

    private File envFile;

    @Override
    public String getDisplayName() {
        return "1-way conformation (1st STG without dummies) [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public int getPriority() {
        return 2;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    public void setEnvironment(File envFile) {
        this.envFile = envFile;
    }

    public File getEnvironment() {
        return envFile;
    }

    @Override
    public void run(WorkspaceEntry we) {
        queueVerification(we, true);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandler monitor = queueVerification(we, false);
        Result<? extends VerificationChainOutput> result = null;
        if (monitor != null) {
            result = monitor.waitResult();
        }
        return MpsatUtils.getChainOutcome(result);
    }

    private VerificationChainResultHandler queueVerification(WorkspaceEntry we, boolean interactive) {
        if (!isApplicableTo(we)) {
            return null;
        }
        VerificationChainResultHandler monitor = null;
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (check(stg)) {
            Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            boolean proceed = true;
            if (interactive) {
                Importer[] importers = {new StgImporter()};
                JFileChooser fc = mainWindow.createOpenDialog("Open environment file", false, true, importers);
                if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
                    setEnvironment(fc.getSelectedFile());
                } else {
                    proceed = false;
                }
            }
            if (proceed && FileUtils.checkAvailability(getEnvironment(), null, true)) {
                Stg envStg = StgUtils.loadStg(getEnvironment());
                if (envStg == null) {
                    DialogUtils.showError("Cannot read an STG model from the file:\n"
                            + getEnvironment().getAbsolutePath() + "\n\n"
                            + "Conformation cannot be checked without environment STG.");
                } else {
                    TaskManager manager = framework.getTaskManager();
                    ConformationTask task = new ConformationTask(we, getEnvironment());
                    String description = MpsatUtils.getToolchainDescription(we.getTitle());
                    monitor = new VerificationChainResultHandler(we);
                    manager.queue(task, description, monitor);
                }
            }
        }
        return monitor;
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
