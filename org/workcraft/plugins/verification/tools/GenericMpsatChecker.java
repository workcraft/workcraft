package org.workcraft.plugins.verification.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.gui.MpsatConfigurationDialog;
import org.workcraft.util.GUI;

@DisplayName("Check custom property (punf, MPSat)")
public class GenericMpsatChecker implements Tool {

	@Override
	public String getSection() {
		return "Verification";
	}

	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG || model instanceof VisualSTG)
			return true;
		else
			return false;
	}

	@Override
	public void run(Model model, Framework framework) {
		MpsatPresetManager pmgr = new MpsatPresetManager();
		MpsatConfigurationDialog dialog = new MpsatConfigurationDialog(framework.getMainWindow(), pmgr);
		dialog.setSize(500, 500);
		GUI.centerFrameToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
	}

}
