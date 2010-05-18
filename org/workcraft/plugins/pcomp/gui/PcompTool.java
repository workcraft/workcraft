package org.workcraft.plugins.pcomp.gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.DotGProvider;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Parallel composition")
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

		if (dialog.run()) {

			DotGProvider dotGProvider = new DotGProvider(framework);

			ArrayList<File> inputs = new ArrayList<File>();

			for (Path<String> p : dialog.getSourcePaths()) {
				inputs.add(dotGProvider.getDotG(p));
			}

			try {
				File result = File.createTempFile("pcompresult", ".g");
				framework.getTaskManager().queue(new PcompTask(inputs.toArray(new File[0])), "Running pcomp", new PcompResultHandler(framework, dialog.showInEditor()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}