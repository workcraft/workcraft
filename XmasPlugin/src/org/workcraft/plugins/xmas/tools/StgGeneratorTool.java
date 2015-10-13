package org.workcraft.plugins.xmas.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.stg.StgGenerator;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgGeneratorTool extends ConversionTool {

	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Xmas;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualXmas xmas = (VisualXmas)we.getModelEntry().getVisualModel();
		final StgGenerator generator = new StgGenerator(xmas);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String desiredName = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new StgDescriptor(), generator.getStgModel());
		boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
		workspace.add(directory, desiredName, me, false, openInEditor);
	}
}
