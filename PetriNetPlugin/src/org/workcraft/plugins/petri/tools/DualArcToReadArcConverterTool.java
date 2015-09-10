package org.workcraft.plugins.petri.tools;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.PetriNetUtils;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class DualArcToReadArcConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected paired producer-consumer arcs to read-arcs";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof PetriNetModel;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualModel visualModel = we.getModelEntry().getVisualModel();
		HashSet<Pair<VisualConnection /* consuming arc */, VisualConnection /* producing arc */>> dualArcs = new HashSet<>();
		HashSet<VisualConnection> consumingArcs = PetriNetUtils.getVisualConsumingArcs(visualModel);
		HashSet<VisualConnection> producingArcs = PetriNetUtils.getVisualProducerArcs(visualModel);
		for (VisualConnection consumingArc: consumingArcs) {
			for (VisualConnection producingArc: producingArcs) {
				boolean isDualArcs = ((consumingArc.getFirst() == producingArc.getSecond())
						&& (consumingArc.getSecond() == producingArc.getFirst()));
				Collection<Node> selection = visualModel.getSelection();
				boolean selectedArcsOrNoSelection = (selection.isEmpty() || selection.contains(consumingArc) || selection.contains(producingArc));
				if (isDualArcs && selectedArcsOrNoSelection) {
					dualArcs.add(new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc));
				}
			}
		}
		if ( !dualArcs.isEmpty() ) {
			we.saveMemento();
			HashSet<VisualReadArc> readArcs = new HashSet<>(dualArcs.size());
			for (Pair<VisualConnection, VisualConnection> dualArc: dualArcs) {
				VisualConnection consumingArc = dualArc.getFirst();
				VisualConnection producingArc = dualArc.getSecond();
				VisualReadArc readArc = PetriNetUtils.convertDualArcToReadArc(visualModel, consumingArc, producingArc);
				if (readArc instanceof VisualReadArc) {
					readArcs.add(readArc);
				}
			}
			visualModel.select(new LinkedList<Node>(readArcs));
		}
	}

}
