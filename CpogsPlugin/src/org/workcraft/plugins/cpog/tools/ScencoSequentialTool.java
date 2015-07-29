package org.workcraft.plugins.cpog.tools;

import java.io.File;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.EncoderSettingsSerialiser;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.gui.ScencoSingleSequentialDialog;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoSequentialTool implements Tool {

	private EncoderSettings settings;
	private ScencoSingleSequentialDialog dialog;
	PresetManager<EncoderSettings> pmgr;

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public String getSection() {
		return "!Encoding";
	}

	@Override
	public String getDisplayName() {
		return "Sequential encoding (trivial)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();
		settings = new EncoderSettings(10, GenerationMode.SEQUENTIAL, false, false);
		pmgr = new PresetManager<>(new File("config/cpog_presets.xml"), new EncoderSettingsSerialiser());
		dialog = new ScencoSingleSequentialDialog(mainWindow, pmgr, settings, we, "Sequential Encoding");

		GUI.centerToParent(dialog, mainWindow);
		dialog.setVisible(true);
		// TASK INSERTION
		if (dialog.getModalResult() == 1) {
			// Instantiate Solver
			final ScencoExternalToolTask scencoTask = new ScencoExternalToolTask(dialog.getSettings(),we,
					new ScencoSolver(dialog.getSettings(), we));
			// Instantiate object for handling solution
			ScencoResultHandler resultScenco = new ScencoResultHandler(scencoTask);
			//Run both
			framework.getTaskManager().queue(scencoTask, "Sequential Encoding execution", resultScenco);
		}
	}

}
