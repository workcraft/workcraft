package org.workcraft.plugins.cpog.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.math.MathModel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CPOG;
import org.workcraft.plugins.cpog.CpogModelDescriptor;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogToGraphConverterTool implements Tool {

	@Override
	public String getDisplayName() {
		return "Directed Graph";
	}

	@Override
	public String getSection() {
		return "Conversion";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		MathModel mathModel = we.getModelEntry().getMathModel();
		return mathModel.getClass().equals(CPOG.class);
	}

	@Override
	public void run(WorkspaceEntry we) {
		we.captureMemento();
		try {
			final VisualCPOG cpog = (VisualCPOG)we.getModelEntry().getVisualModel();
			final VisualGraph graph = new VisualGraph(new Graph());
			final CpogToGraphConverter converter = new CpogToGraphConverter(cpog, graph);
			final Framework framework = Framework.getInstance();
			final Workspace workspace = framework.getWorkspace();
			final Path<String> directory = we.getWorkspacePath().getParent();
			final String name = we.getWorkspacePath().getNode();
			final ModelEntry me = new ModelEntry(new CpogModelDescriptor(), converter.getDstModel());
			workspace.add(directory, name, me, false, true);
		} finally {
			we.cancelMemento();
		}
	}

}
