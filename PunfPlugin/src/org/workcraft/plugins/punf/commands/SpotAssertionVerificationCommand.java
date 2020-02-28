package org.workcraft.plugins.punf.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractVerificationCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.punf.PunfSettings;
import org.workcraft.plugins.punf.tasks.SpotChainResultHandler;
import org.workcraft.plugins.punf.tasks.SpotChainTask;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.presets.PresetManager;
import org.workcraft.presets.TextDataSerialiser;
import org.workcraft.presets.TextPresetDialog;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ScriptableCommandUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SpotAssertionVerificationCommand extends AbstractVerificationCommand {

    private static final String PRESET_KEY = "spot-assertions.xml";
    private static final TextDataSerialiser DATA_SERIALISER = new TextDataSerialiser();

    private static String preservedData = null;

    @Override
    public String getDisplayName() {
        return "SPOT assertion [LTL2TGBA]...";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public boolean isVisibleInMenu() {
        return PunfSettings.getShowSpotInMenu();
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM;
    }

    @Override
    public Boolean execute(WorkspaceEntry we) {
        ScriptableCommandUtils.showErrorRequiresGui(getClass().getSimpleName());
        return null;
    }

    @Override
    public void run(WorkspaceEntry we) {
        Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        PresetManager<String> presetManager = new PresetManager<>(we, PRESET_KEY, DATA_SERIALISER, preservedData);
        presetManager.addExample("Every request is acknowledged", "G(\"req\" -> (\"ack\" M \"req\"))");
        presetManager.addExample("Mutual exclusion of signals", "G((!\"u\") | (!\"v\"))");

        TextPresetDialog dialog = new TextPresetDialog(mainWindow, "SPOT assertion", presetManager);
        if (dialog.reveal()) {
            preservedData = dialog.getPresetData();
            TaskManager manager = framework.getTaskManager();
            SpotChainTask task = new SpotChainTask(we, preservedData);
            SpotChainResultHandler monitor = new SpotChainResultHandler(we);
            manager.queue(task, "Running SPOT assertion [LTL2TGBA]", monitor);
        }
    }

}
