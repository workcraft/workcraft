package org.workcraft.plugins.mpsat.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class SignalAssertionVerificationCommand extends org.workcraft.commands.AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, String> {

    private static final String PRESET_KEY = "signal-assertions.xml";
    private static final TextDataSerialiser DATA_SERIALISER = new TextDataSerialiser();

    private static String preservedData = null;

    @Override
    public String getDisplayName() {
        return "Signal assertion [MPSat]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, StgModel.class);
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        PresetManager<String> presetManager = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);
        presetManager.addExample("Mutual exclusion of signals",
                "// Signals u and v are mutually exclusive\n"
                        + "!u || !v");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "Signal assertion",
                presetManager, new File("help/assertion.html"));

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
            run(we, preservedData, monitor);
        }
    }

    @Override
    public void run(WorkspaceEntry we, String data, ProgressMonitor monitor) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationParameters verificationParameters = new VerificationParameters(null,
                VerificationMode.ASSERTION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                data, true);

        VerificationChainTask task = new VerificationChainTask(we, verificationParameters);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        manager.queue(task, description, monitor);
    }

    @Override
    public String deserialiseData(String str) {
        return str;
    }

    @Override
    public Boolean execute(WorkspaceEntry we, String data) {
        VerificationChainResultHandler monitor = new VerificationChainResultHandler(we);
        run(we, data, monitor);
        return MpsatUtils.getChainOutcome(monitor);
    }

}
