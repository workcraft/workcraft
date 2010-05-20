package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.GUI;

import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.gui.DesiJConfigurationDialog;
import org.workcraft.plugins.desij.tasks.DesiJTask;

@DisplayName("DesiJ - customise function")
public class DesiJCustomFunction implements Tool {

	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	@Override
	public void run(Model model, Framework framework) {
		DesiJPresetManager pmgr = new DesiJPresetManager();
		DesiJConfigurationDialog dialog = new DesiJConfigurationDialog(framework.getMainWindow(), pmgr);
		GUI.centerFrameToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1)
		{
			framework.getTaskManager().queue(new DesiJTask(model, framework, dialog.getSettings()),
					"DesiJ Execution", new DecompositionResultHandler(framework));
		}
	}

}
