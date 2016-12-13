package org.workcraft.plugins.mpsat.tools;

import java.io.File;

import org.workcraft.AbstractVerificationCommand;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.gui.MpsatAssertionDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class AssertionVerificationCommand extends AbstractVerificationCommand {

    public static final String MPSAT_ASSERTION_PRESETS_FILE = "mpsat-assertion-presets.xml";

    @Override
    public String getDisplayName() {
        return "Custom assertion [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        return null; // !!!
    }

    @Override
    public WorkspaceEntry run(WorkspaceEntry we) {
        File presetFile = new File(Framework.SETTINGS_DIRECTORY_PATH, MPSAT_ASSERTION_PRESETS_FILE);
        MpsatPresetManager pmgr = new MpsatPresetManager(presetFile, new MpsatSettingsSerialiser(), true);
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        MpsatAssertionDialog dialog = new MpsatAssertionDialog(mainWindow, pmgr);
        dialog.pack();
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            final MpsatChainTask mpsatTask = new MpsatChainTask(we, dialog.getSettings());
            final TaskManager taskManager = framework.getTaskManager();
            final MpsatChainResultHandler monitor = new MpsatChainResultHandler(mpsatTask);
            taskManager.queue(mpsatTask, "MPSat tool chain", monitor);
        }
        return we;
    }

}
