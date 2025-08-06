package org.workcraft.plugins.mpsat_verification.commands;

import org.workcraft.Framework;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.mpsat_verification.MpsatVerificationSettings;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.mpsat_verification.utils.MpsatUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class SignalAssertionVerificationCommand
        extends org.workcraft.commands.AbstractVerificationCommand
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
        presetManager.addExamplePreset("Mutual exclusion of signals",
                "// Signals u and v are mutually exclusive\n" + "!u || !v");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "Signal assertion", presetManager);
        dialog.addHelpButton(new File("help/assertion.html"));
        dialog.addCheckerButton(event -> MpsatUtils.checkSyntax(we, dialog.getCodePanel(),
                convertDataToVerificationParameters(dialog.getCodePanel().getText())));

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            queueTask(we, preservedData);
        }
    }

    private VerificationChainResultHandlingMonitor queueTask(WorkspaceEntry we, String data) {
        TaskManager manager = Framework.getInstance().getTaskManager();
        VerificationParameters verificationParameters = convertDataToVerificationParameters(data);

        VerificationChainTask task = new VerificationChainTask(we, verificationParameters);
        String description = MpsatUtils.getToolchainDescription(we.getTitle());
        VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we);
        manager.queue(task, description, monitor);
        return monitor;
    }

    private VerificationParameters convertDataToVerificationParameters(String data) {
        return new VerificationParameters(null,
                VerificationMode.ASSERTION, 0,
                MpsatVerificationSettings.getSolutionMode(),
                MpsatVerificationSettings.getSolutionCount(),
                data, true);
    }

    @Override
    public String deserialiseData(String data) {
        return data;
    }

    @Override
    public Boolean execute(WorkspaceEntry we, String data) {
        VerificationChainResultHandlingMonitor monitor = queueTask(we, data);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

}
