package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.AssertionCheckTask;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.plugins.mpsat.VerificationMode;
import org.workcraft.plugins.mpsat.VerificationParameters;
import org.workcraft.plugins.mpsat.tasks.VerificationChainResultHandler;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class SignalAssertionVerificationCommand extends AbstractVerificationCommand
        implements ScriptableCommand<Boolean> {

    private static final String PRESET_KEY = "signal-assertions.xml";
    private static final TextDataSerialiser DATA_SERIALISER = new TextDataSerialiser();

    private static String preservedData = null;

    @Override
    public String getDisplayName() {
        return "Signal assertion [MPSat]...";
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
        PresetManager<String> presetManager = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);

        presetManager.addExample("Mutual exclusion of signals",
                "// Signals u and v are mutually exclusive\n"
                        + "!u || !v");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "Signal assertion",
                presetManager, new File("help/assertion.html"));

        if (dialog.reveal()) {
            TaskManager manager = framework.getTaskManager();
            preservedData = dialog.getPresetData();
            VerificationParameters verificationParameters = new VerificationParameters(null,
                    VerificationMode.ASSERTION, 0, VerificationParameters.SolutionMode.MINIMUM_COST,
                    0, preservedData, true);

            AssertionCheckTask task = new AssertionCheckTask(we, verificationParameters);
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