package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatDataSerialiser;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.gui.ReachAssertionDialog;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.StgModel;
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
        return WorkspaceUtils.isApplicable(we, PetriModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        boolean allowStgPresets = WorkspaceUtils.isApplicable(we, StgModel.class);
        MpsatPresetManager presetManager = new MpsatPresetManager(we, PRESET_KEY, DATA_SERIALISER, allowStgPresets, preservedData);
        ReachAssertionDialog dialog = new ReachAssertionDialog(mainWindow, presetManager);
        if (dialog.reveal()) {
            TaskManager manager = framework.getTaskManager();
            preservedData = dialog.getPresetData();
            VerificationChainTask task = new VerificationChainTask(we, preservedData);
            String description = MpsatUtils.getToolchainDescription(we.getTitle());
            VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
            manager.queue(task, description, monitor);
        }
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        ScriptableCommandUtils.showErrorRequiresGui(getClass());
        return null;
    }

}
