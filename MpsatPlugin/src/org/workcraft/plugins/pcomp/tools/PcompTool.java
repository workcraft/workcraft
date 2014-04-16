package org.workcraft.plugins.pcomp.tools;

import java.io.File;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Tool;
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

	private final Framework framework;

	public PcompTool(Framework framework) {
		this.framework = framework;
	}

	public final String getSection() {
		return "Composition";
	}

	public final boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, STGModel.class);
	}

	public final void run(WorkspaceEntry we) {
		PcompDialog dialog = new PcompDialog(framework.getMainWindow(), framework);
		GUI.centerAndSizeToParent(dialog, framework.getMainWindow());

		if (dialog.run()) {

			DotGProvider dotGProvider = new DotGProvider(framework);

			ArrayList<File> inputs = new ArrayList<File>();

			for (Path<String> p : dialog.getSourcePaths()) {
				inputs.add(dotGProvider.getDotG(p));
			}

			framework.getTaskManager().queue(new PcompTask(inputs.toArray(new File[0]), dialog.getMode(), dialog.isImprovedPcompChecked()), "Running pcomp", new PcompResultHandler(framework, dialog.showInEditor()));
		}
	}

	@Override
	public String getDisplayName() {
		return "Parallel composition [PComp]";
	}
}