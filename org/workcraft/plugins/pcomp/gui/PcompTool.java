package org.workcraft.plugins.pcomp.gui;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.GUI;

public class PcompTool implements Tool {

	public final String getSection() {
		return "Tools";
	}

	public final boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	public final void run(Model model, Framework framework) {
		PcompDialog dialog = new PcompDialog(framework.getMainWindow(), framework);
		GUI.centerFrameToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
	}
}
