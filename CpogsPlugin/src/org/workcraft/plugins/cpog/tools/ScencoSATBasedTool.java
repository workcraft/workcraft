package org.workcraft.plugins.cpog.tools;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.EncoderSettingsSerialiser;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.gui.ScencoSatBasedDialog;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoSATBasedTool implements Tool {

	private EncoderSettings settings;
	private ScencoSatBasedDialog dialog;
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
		return "SAT-based optimal encoding";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();
		if ( !CpogParsingTool.hasEnoughScenarios(we) ) {
			JOptionPane.showMessageDialog(mainWindow, ScencoSolver.MSG_NOT_ENOUGH_SCENARIOS,
					ScencoSolver.TITLE_SCENCO_ERROR, JOptionPane.ERROR_MESSAGE);
		} else {
			settings = new EncoderSettings(10, GenerationMode.SCENCO, false, false);
			pmgr = new PresetManager<>(new File("config/cpog_presets.xml"), new EncoderSettingsSerialiser());
			dialog = new ScencoSatBasedDialog(mainWindow, pmgr, settings, we);

			GUI.centerToParent(dialog, mainWindow);
			dialog.setVisible(true);
			// TASK INSERTION
			/*final ScencoChainTask scencoTask = new ScencoChainTask(we, dialog.getSettings(), framework);
			framework.getTaskManager().queue(scencoTask, "Scenco tool chain", new ScencoChainResultHandler(scencoTask));*/
		}
	}

}
