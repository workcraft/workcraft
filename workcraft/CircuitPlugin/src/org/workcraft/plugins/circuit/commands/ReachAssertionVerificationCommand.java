package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.AssertionCheckTask;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.MpsatDataSerialiser;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.gui.ReachAssertionDialog;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachAssertionVerificationCommand extends AbstractVerificationCommand {

    private static final String PRESET_KEY = "reach-assertions.xml";
    private static final MpsatDataSerialiser DATA_SERIALISER = new MpsatDataSerialiser();

    private static VerificationParameters preservedData = null;

    @Override
    public String getDisplayName() {
        return "REACH assertion [MPSat]...";
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
        ScriptableCommandUtils.showErrorRequiresGui(getClass());
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        if (!checkPrerequisites(we)) {
            return;
        }
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        MpsatPresetManager pmgr = new MpsatPresetManager(we, PRESET_KEY, DATA_SERIALISER, true, preservedData);
        ReachAssertionDialog dialog = new ReachAssertionDialog(mainWindow, pmgr);
        if (dialog.reveal()) {
            TaskManager manager = framework.getTaskManager();
            preservedData = dialog.getPresetData();
            AssertionCheckTask task = new AssertionCheckTask(we, preservedData);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
            manager.queue(task, description, monitor);
        }
    }

    private boolean checkPrerequisites(WorkspaceEntry we) {
        return isApplicableTo(we)
            && VerificationUtils.checkCircuitHasComponents(we)
            && VerificationUtils.checkInterfaceInitialState(we)
            && VerificationUtils.checkInterfaceConstrains(we);
    }

}
