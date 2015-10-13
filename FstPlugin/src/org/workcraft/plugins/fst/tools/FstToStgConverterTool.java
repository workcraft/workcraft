package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class FstToStgConverterTool extends ConversionTool {

	@Override
	public String getDisplayName() {
		return "Signal Transition Graph";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Fst;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualFst fst = (VisualFst)we.getModelEntry().getVisualModel();
		final VisualSTG stg = new VisualSTG(new STG());
		final FstToStgConverter converter = new FstToStgConverter(fst, stg);
		final Framework framework = Framework.getInstance();
		final Workspace workspace = framework.getWorkspace();
		final Path<String> directory = we.getWorkspacePath().getParent();
		final String desiredName = we.getWorkspacePath().getNode();
		final ModelEntry me = new ModelEntry(new StgDescriptor(), converter.getDstModel());
		boolean openInEditor = (me.isVisual() || CommonEditorSettings.getOpenNonvisual());
		workspace.add(directory, desiredName, me, false, openInEditor);
	}
}
