package org.workcraft.plugins.circuit.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.commands.ScriptableDataCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.tasks.SpotChainTask;
import org.workcraft.plugins.mpsat_temporal.MpsatTemporalSettings;
import org.workcraft.plugins.mpsat_temporal.tasks.SpotChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_temporal.utils.SpotUtils;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;

public class SpotAssertionVerificationCommand extends AbstractVerificationCommand
        implements ScriptableDataCommand<Boolean, String> {

    private static final String PRESET_KEY = "spot-assertions.xml";
    private static final TextDataSerialiser DATA_SERIALISER = new TextDataSerialiser();

    private static String preservedData = null;

    @Override
    public String getDisplayName() {
        return "SPOT assertion [LTL2TGBA]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Circuit.class);
    }

    @Override
    public Visibility getVisibility() {
        return MpsatTemporalSettings.getShowSpotInMenu() ? Visibility.APPLICABLE : Visibility.NEVER;
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        PresetManager<String> presetManager = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);
        presetManager.addExamplePreset("Every request is acknowledged", "G(\"req\" -> (\"ack\" M \"req\"))");
        presetManager.addExamplePreset("Mutual exclusion of signals", "G((!\"u\") | (!\"v\"))");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "SPOT assertion", presetManager);
        dialog.addHelpButton(new File("https://spot.lrde.epita.fr/tl.pdf"));
        dialog.addCheckerButton(event -> SpotUtils.checkSyntax(we, dialog.getCodePanel()));

        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            queueTask(we, preservedData);
        }
    }

    private SpotChainResultHandlingMonitor queueTask(WorkspaceEntry we, String data) {
        Framework framework = Framework.getInstance();
        TaskManager manager = framework.getTaskManager();
        SpotChainTask task = new SpotChainTask(we, data);
        SpotChainResultHandlingMonitor monitor = new SpotChainResultHandlingMonitor(we);
        manager.queue(task, "Running SPOT assertion [LTL2TGBA]", monitor);
        return monitor;
    }

    @Override
    public String deserialiseData(String data) {
        return data;
    }

    @Override
    public Boolean execute(WorkspaceEntry we, String data) {
        SpotChainResultHandlingMonitor monitor = queueTask(we, data);
        monitor.setInteractive(false);
        return monitor.waitForHandledResult();
    }

}
