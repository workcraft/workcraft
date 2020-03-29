package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.interop.Format;
import org.workcraft.plugins.mpsat.tasks.ConformationTask;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgFormat;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.File;

public class ConformationVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

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
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
        queueVerification(we, monitor);
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        queueVerification(we, monitor);
        return monitor.waitForHandledResult();
    }

    private void queueVerification(WorkspaceEntry we, VerificationChainResultHandlingMonitor monitor) {
        if (!isApplicableTo(we)) {
            monitor.isFinished(Result.failure());
            return;
        }

        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        if (!check(stg)) {
            monitor.isFinished(Result.failure());
            return;
        }

        Framework framework = Framework.getInstance();
        boolean proceed = true;
        if (monitor.isInteractive()) {
            MainWindow mainWindow = framework.getMainWindow();
            Format[] formats = {StgFormat.getInstance()};
            JFileChooser fc = mainWindow.createOpenDialog("Open environment file", false, true, formats);
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
                manager.queue(task, description, monitor);
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
