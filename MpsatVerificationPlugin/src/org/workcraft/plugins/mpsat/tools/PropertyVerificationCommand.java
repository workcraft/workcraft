package org.workcraft.plugins.mpsat.tools;

import java.io.File;

import org.workcraft.AbstractVerificationCommand;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.gui.MpsatPropertyDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PropertyVerificationCommand extends AbstractVerificationCommand {

    public static final String MPSAT_PROPERTY_PRESETS_FILE = "mpsat-property-presets.xml";

    @Override
    public String getDisplayName() {
        return "Custom property [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PetriNetModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public final void run(WorkspaceEntry we) {
        File presetFile = new File(Framework.SETTINGS_DIRECTORY_PATH, MPSAT_PROPERTY_PRESETS_FILE);
        boolean allowStgPresets = WorkspaceUtils.isApplicable(we, StgModel.class);
        MpsatPresetManager pmgr = new MpsatPresetManager(presetFile, new MpsatSettingsSerialiser(), allowStgPresets);
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        MpsatPropertyDialog dialog = new MpsatPropertyDialog(mainWindow, pmgr);
        dialog.pack();
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            final MpsatChainTask mpsatTask = new MpsatChainTask(we, dialog.getSettings());
            final TaskManager taskManager = framework.getTaskManager();
            final MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
            taskManager.queue(mpsatTask, "MPSat tool chain", monitor);
        }
    }

}
