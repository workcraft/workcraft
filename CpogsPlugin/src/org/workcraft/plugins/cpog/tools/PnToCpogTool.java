package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.PnToCpogSettings;
import org.workcraft.plugins.cpog.gui.PnToCpogDialog;
import org.workcraft.plugins.cpog.tasks.PnToCpogHandler;
import org.workcraft.plugins.cpog.tasks.PnToCpogTask;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PnToCpogTool extends ConversionTool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNet.class);
	}

	@Override
	public String getDisplayName() {
		return "Conditional Partial Order Graph [Untanglings]";
	}

	@Override
	public void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();

		PnToCpogSettings settings = new PnToCpogSettings();
		PnToCpogDialog dialog = new PnToCpogDialog(mainWindow, settings, we);

		GUI.centerToParent(dialog, mainWindow);
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1) {
			// Instantiate Solver
			PnToCpogTask task = new PnToCpogTask(we, settings);
			// Instantiate object for handling solution
			PnToCpogHandler result = new PnToCpogHandler(task);
			//Run both
			framework.getTaskManager().queue(task, "Converting Petri net into CPOG...", result);
		}
	}
}
