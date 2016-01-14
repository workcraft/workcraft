package org.workcraft.plugins.petri.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.NodeTransformer;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.workspace.WorkspaceEntry;

public class ConsumingArcToReadArcConverterTool extends TransformationTool implements NodeTransformer {
	private HashSet<VisualReadArc> readArcs = null;

	@Override
	public String getDisplayName() {
		return "Convert selected consuming arcs to a read-arcs";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return (we.getModelEntry().getMathModel() instanceof PetriNetModel);
	}

	@Override
	public boolean isApplicableTo(Node node) {
		return PetriNetUtils.isVisualConsumingArc(node);
	};

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel model = we.getModelEntry().getVisualModel();
		HashSet<VisualConnection> connections = PetriNetUtils.getVisualConsumingArcs(model);
		connections.retainAll(model.getSelection());
		if ( !connections.isEmpty() ) {
			we.saveMemento();
			readArcs = new HashSet<>();
			for (VisualConnection connection: connections) {
				transform(model, connection);
			}
			model.select(new LinkedList<Node>(readArcs));
			readArcs = null;
		}
	}

	@Override
	public void transform(Model model, Node node) {
		if ((model instanceof VisualModel) && (node instanceof VisualConnection)) {
			VisualConnection connection = (VisualConnection)node;
			VisualReadArc readArc = PetriNetUtils.convertDirectedArcToReadArc((VisualModel)model, connection);
			if ((readArcs != null) && (readArc != null)) {
				readArcs.add(readArc);
			}
		}
	}

}
