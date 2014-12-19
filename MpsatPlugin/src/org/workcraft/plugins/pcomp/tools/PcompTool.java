package org.workcraft.plugins.pcomp.tools;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.pcomp.gui.PcompDialog;
import org.workcraft.plugins.pcomp.tasks.PcompResultHandler;
import org.workcraft.plugins.pcomp.tasks.PcompTask;
import org.workcraft.plugins.stg.DotGProvider;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompTool implements Tool {

	public final String getSection() {
		return "Composition";
	}

	public final boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	public final void run(WorkspaceEntry we) {
		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();
		PcompDialog dialog = new PcompDialog(mainWindow);
		GUI.centerAndSizeToParent(dialog, mainWindow);
		if (dialog.run()) {
			DotGProvider dotGProvider = new DotGProvider();
			ArrayList<File> inputs = new ArrayList<File>();
			for (Path<String> p : dialog.getSourcePaths()) {
				inputs.add(dotGProvider.getDotG(p));
			}
			PcompTask pcompTask = new PcompTask(inputs.toArray(new File[0]), dialog.getMode(),
					dialog.isSharedOutputsChecked(), dialog.isImprovedPcompChecked(), null);

			PcompResultHandler pcompResult = new PcompResultHandler(dialog.showInEditor());
			framework.getTaskManager().queue(pcompTask,	"Running pcomp", pcompResult);
		}
	}

	@Override
	public String getDisplayName() {
		return "Parallel composition [PComp]";
	}
}