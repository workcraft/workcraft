package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.gui.DesiJConfigurationDialog;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class DesiJCustomFunction implements Tool {

	private final Framework framework;

	public DesiJCustomFunction(Framework framework){
		this.framework = framework;
	}

	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		DesiJPresetManager pmgr = new DesiJPresetManager();
		DesiJConfigurationDialog dialog = new DesiJConfigurationDialog(framework.getMainWindow(), pmgr);
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1)
		{
			framework.getTaskManager().queue(new DesiJTask(WorkspaceUtils.getAs(we, STGModel.class), framework, dialog.getSettings()),
					"DesiJ Execution", new DecompositionResultHandler(framework, false));
		}
	}

	@Override
	public String getDisplayName() {
		return "Customised function (DesiJ)";
	}
}