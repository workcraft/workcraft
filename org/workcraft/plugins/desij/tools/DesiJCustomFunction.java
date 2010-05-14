package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.shared.MpsatChainResultHandler;
import org.workcraft.plugins.shared.MpsatPresetManager;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.verification.gui.MpsatConfigurationDialog;
import org.workcraft.util.GUI;

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
//		MpsatPresetManager pmgr = new MpsatPresetManager();
//		DesiJConfigurationDialog dialog = new DesiJConfigurationDialog(framework.getMainWindow(), pmgr);
//		GUI.centerFrameToParent(dialog, framework.getMainWindow());
//		dialog.setVisible(true);
//		if (dialog.getModalResult() == 1)
//		{
//			framework.getTaskManager().queue(new MpsatChainTask(model, dialog.getSettings(), framework), "MPSat tool chain",
//					new MpsatChainResultHandler());
//		}
	}

}
