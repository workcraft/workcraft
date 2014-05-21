package org.workcraft.plugins.son.tools;

import org.apache.log4j.Logger;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.son.OutputRedirect;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.gui.StructureVerifyDialog;
import org.workcraft.plugins.son.verify.SONMainTask;
import org.workcraft.plugins.son.verify.TSONMainTask;
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
		return "Check structure property";
	}

	public void run(WorkspaceEntry we){

		SONModel net=(SONModel)we.getModelEntry().getMathModel();
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();

		StructureVerifyDialog dialog = new StructureVerifyDialog(framework.getMainWindow(), net);
		GUI.centerToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);

		if (dialog.getRun() == 1){
			//Change connections from block inside to bounding.
			if(!vnet.connectToBlocks())
				return;

			OutputRedirect.Redirect();

			SONMainTask sonTask = new SONMainTask(dialog.getSetting(), net);
			framework.getTaskManager().execute(sonTask, "Verification");
			//Change connections from block bounding to inside.
			vnet.connectToBlocksInside();

			TSONMainTask tsonTask = new TSONMainTask(dialog.getSetting(), net);
			framework.getTaskManager().execute(tsonTask, "Verification");

			int err = sonTask.getTotalErrNum() + tsonTask.getTotalErrNum();
			int warning = sonTask.getTotalWarningNum() + tsonTask.getTotalWarningNum();

			logger.info("\n\nVerification-Result : "+ err + " Error(s), " + warning + " Warning(s).");
		}
	}
}
