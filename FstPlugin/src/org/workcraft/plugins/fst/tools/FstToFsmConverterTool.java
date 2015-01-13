package org.workcraft.plugins.fst.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmModelDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class FstToFsmConverterTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Finite State Machine";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		MathModel mathModel = we.getModelEntry().getMathModel();
		return mathModel.getClass().equals(Fst.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		we.captureMemento();
		try {
			final VisualFst fst = (VisualFst)we.getModelEntry().getVisualModel();
			final VisualFsm fsm = new VisualFsm(new Fsm());
			final FstToFsmConverter converter = new FstToFsmConverter(fst, fsm);
			final Framework framework = Framework.getInstance();
			final Workspace workspace = framework.getWorkspace();
			final Path<String> directory = we.getWorkspacePath().getParent();
			final String name = we.getWorkspacePath().getNode();
			final ModelEntry me = new ModelEntry(new FsmModelDescriptor(), converter.getDstModel());
			workspace.add(directory, name, me, false, true);
		} finally {
			we.cancelMemento();
		}
	}

}
