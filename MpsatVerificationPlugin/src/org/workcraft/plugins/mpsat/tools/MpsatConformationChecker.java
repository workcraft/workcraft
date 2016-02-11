package org.workcraft.plugins.mpsat.tools;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.VerificationTool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatConformationTask;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatConformationChecker extends VerificationTool {

    @Override
    public String getDisplayName() {
        return "   Conformation (without dummies) [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, STGModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        STG stg = (STG)we.getModelEntry().getMathModel();
        // Check for limitations:
        if (stg.getPlaces().isEmpty()) {
            // - The set of device STG place names is non-empty (this limitation can be easily removed).
            JOptionPane.showMessageDialog(mainWindow, "The STG must have places.",
                    "Conformation check error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (hasDisconnectedTransitions(stg)) {
            // - Each transition in the device STG must have some arcs, i.e. its preset or postset is non-empty.
            JOptionPane.showMessageDialog(mainWindow, "The STG must have no disconnected transitions.",
                    "Conformation check error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!stg.getDummyTransitions().isEmpty()) {
            // - The device STG must have no dummies.
            JOptionPane.showMessageDialog(mainWindow, "The STG must have no dummies.",
                    "Conformation check error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fc = framework.getMainWindow().createOpenDialog("Open environment file", false, null);
        if (fc.showDialog(null, "Open") == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (framework.checkFile(file, null)) {
                final MpsatConformationTask mpsatTask = new MpsatConformationTask(we, file);

                String description = "MPSat tool chain";
                String title = we.getTitle();
                if (!title.isEmpty()) {
                    description += "(" + title +")";
                }
                MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
                framework.getTaskManager().queue(mpsatTask, description, monitor);
            }
        }
    }

    private boolean hasDisconnectedTransitions(STG stg) {
        for (Transition t: stg.getTransitions()) {
            if (stg.getPreset(t).isEmpty() && stg.getPostset(t).isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
