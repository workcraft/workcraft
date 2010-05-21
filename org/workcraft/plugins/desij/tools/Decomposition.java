package org.workcraft.plugins.desij.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.desij.tasks.DesiJTask;

import org.workcraft.plugins.desij.DecompositionResultHandler;

@DisplayName("DesiJ - default decomposition")
public class Decomposition implements Tool {

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

		// call desiJ asynchronous (w/o blocking the GUI)
		framework.getTaskManager().queue(new DesiJTask(model, framework, new String[0]),
				"Execution of DesiJ", new DecompositionResultHandler(framework, true));

	}

}
