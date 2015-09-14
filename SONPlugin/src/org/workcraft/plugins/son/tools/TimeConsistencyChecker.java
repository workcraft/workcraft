package org.workcraft.plugins.son.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.TimeConsistencySettings;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog;
import org.workcraft.plugins.son.tasks.TimeConsistencyTask;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TimeConsistencyChecker implements Tool{

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	@Override
	public String getSection(){
		return "Time analysis";
	}

	@Override
	public String getDisplayName() {
		return "Consistency...";
	}

	@Override
	public void run(WorkspaceEntry we) {
		SON net=(SON)we.getModelEntry().getMathModel();

		final Framework framework = Framework.getInstance();
		final MainWindow mainWindow = framework.getMainWindow();

		TimeConsistencyDialog dialog = new TimeConsistencyDialog(mainWindow, net);
		GUI.centerToParent(dialog, mainWindow);
		dialog.setVisible(true);

		TimeConsistencySettings settings = new TimeConsistencySettings(false, null, null, null);
		if (dialog.getRun() == 1){
			OutputRedirect.Redirect(30, 48);
			TimeConsistencyTask timeTask = new TimeConsistencyTask(we, settings);
			framework.getTaskManager().queue(timeTask, "Verification");
		}
	}

}
