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
import org.workcraft.plugins.cpog.gui.ScencoConstrainedSearchDialog;
import org.workcraft.plugins.cpog.tasks.ScencoExternalToolTask;
import org.workcraft.plugins.cpog.tasks.ScencoResultHandler;
import org.workcraft.plugins.cpog.tasks.ScencoSolver;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

public class ScencoHeuristicTool implements Tool {

	private EncoderSettings settings;
	private ScencoConstrainedSearchDialog dialog;
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
		return "Heuristic encoding (supports constraints)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();
		if ( !CpogParsingTool.hasEnoughScenarios(we) ) {
			JOptionPane.showMessageDialog(mainWindow, ScencoSolver.MSG_NOT_ENOUGH_SCENARIOS,
					ScencoSolver.ACCESS_SCENCO_ERROR, JOptionPane.ERROR_MESSAGE);
		} else {
			settings = new EncoderSettings(10, GenerationMode.OPTIMAL_ENCODING, false, false);
			pmgr = new PresetManager<>(new File("config/cpog_presets.xml"), new EncoderSettingsSerialiser());
			dialog = new ScencoConstrainedSearchDialog(mainWindow, pmgr, settings, we, "Heuristic encoding", 0);

			GUI.centerToParent(dialog, mainWindow);
			dialog.setVisible(true);
			// TASK INSERTION
			if (dialog.getModalResult() == 1) {
				//dialog.getEncoder();

				// Instantiate Solver
				ScencoSolver solver = new ScencoSolver(dialog.getSettings(), we);
				final ScencoExternalToolTask scencoTask = new ScencoExternalToolTask(we, solver);
				// Instantiate object for handling solution
				ScencoResultHandler resultScenco = new ScencoResultHandler(scencoTask);
				//Run both
				framework.getTaskManager().queue(scencoTask, "Heuristic encoding execution", resultScenco);
			}
		}
	}

}
