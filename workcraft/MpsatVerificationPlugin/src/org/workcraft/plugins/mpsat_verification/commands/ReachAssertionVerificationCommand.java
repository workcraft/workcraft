package org.workcraft.plugins.mpsat_verification.commands;

import org.w3c.dom.Document;
import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.*;
import org.workcraft.plugins.mpsat_verification.gui.ReachAssertionDialog;
import org.workcraft.plugins.mpsat_verification.presets.MpsatDataSerialiser;
import org.workcraft.plugins.mpsat_verification.presets.MpsatPresetManager;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.petri.PetriModel;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.presets.PresetManager;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class ReachAssertionVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, VerificationParameters> {

    private static final String PRESET_KEY = "reach-assertions.xml";
    private static final MpsatDataSerialiser DATA_SERIALISER = new MpsatDataSerialiser();
    private static final String DEFAULT_DESCRIPTION = "Custom REACH assertion";

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
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        boolean allowStgPresets = WorkspaceUtils.isApplicable(we, StgModel.class);
        MpsatPresetManager presetManager = new MpsatPresetManager(we, PRESET_KEY, DATA_SERIALISER, allowStgPresets, preservedData);
        ReachAssertionDialog dialog = new ReachAssertionDialog(mainWindow, presetManager);
        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
            run(we, preservedData, monitor);
        }
    }

    @Override
    public void run(WorkspaceEntry we, VerificationParameters data, ProgressMonitor monitor) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationChainTask task = new VerificationChainTask(we, data);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    @Override
    public VerificationParameters deserialiseData(String data) {
        if (data.startsWith("<") && data.endsWith(">")) {
            Document document = PresetManager.buildPresetDocumentFromSettings(DEFAULT_DESCRIPTION, data);
            return DATA_SERIALISER.fromXML(document.getDocumentElement());
        }
        return new VerificationParameters(DEFAULT_DESCRIPTION,
                VerificationMode.STG_REACHABILITY, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                data, true);
    }


    @Override
    public Boolean execute(WorkspaceEntry we, VerificationParameters data) {
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, false);
        run(we, data, monitor);
        return monitor.waitForHandledResult();
    }

}
