package org.workcraft.plugins.son.tools;

import org.apache.log4j.Logger;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.gui.StructureVerifyDialog;
import org.workcraft.plugins.son.tasks.SONMainTask;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class StructurePropertyChecker implements Tool {

	private final Framework framework;
	private Logger logger = Logger.getLogger(this.getClass().getName());

	public StructurePropertyChecker(Framework framework){

		this.framework = framework;

	}

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);
	}

	public String getSection(){
		return "Verification";
	}

	public String getDisplayName(){
		return "Check for structural properties...";
	}

	public void run(WorkspaceEntry we){

		SON net=(SON)we.getModelEntry().getMathModel();
		StructureVerifyDialog dialog = new StructureVerifyDialog(framework.getMainWindow(), net);
		GUI.centerToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);

		if (dialog.getRun() == 1){
			OutputRedirect.Redirect();
			SONMainTask sonTask = new SONMainTask(dialog.getSetting(), we);
			framework.getTaskManager().execute(sonTask, "Verification");

			int err = sonTask.getTotalErrNum();
			int warning = sonTask.getTotalWarningNum();

			logger.info("\n\nVerification-Result : "+ err + " Error(s), " + warning + " Warning(s).");
		}
	}
}
