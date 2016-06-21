package org.workcraft.plugins.mpsat.tools;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.MpsatPresetManager;
import org.workcraft.plugins.mpsat.MpsatSettingsSerialiser;
import org.workcraft.plugins.mpsat.gui.MpsatSvaPropertyDialog;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSvaPropertyChecker extends MpsatPropertyChecker {

    public static final String MPSAT_SVA_PRESETS_FILE = "mpsat-sva-presets.xml";

    @Override
    public String getDisplayName() {
        return "Custom SVA properties [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public void run(WorkspaceEntry we) {
        File presetFile = new File(Framework.SETTINGS_DIRECTORY_PATH, MPSAT_SVA_PRESETS_FILE);
        MpsatPresetManager pmgr = new MpsatPresetManager(presetFile, new MpsatSettingsSerialiser(), true);
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        MpsatSvaPropertyDialog dialog = new MpsatSvaPropertyDialog(mainWindow, pmgr);
        dialog.pack();
        GUI.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() == 1) {
            final MpsatChainTask mpsatTask = new MpsatChainTask(we, dialog.getSettings());
            framework.getTaskManager().queue(mpsatTask, "MPSat tool chain",
                    new MpsatChainResultHandler(mpsatTask));
        }
    }

}
