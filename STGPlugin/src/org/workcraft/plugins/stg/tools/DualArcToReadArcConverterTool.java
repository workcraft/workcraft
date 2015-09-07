package org.workcraft.plugins.stg.tools;

import java.util.HashSet;
import java.util.LinkedList;

import org.workcraft.TransformationTool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualReadArc;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class DualArcToReadArcConverterTool extends TransformationTool {

	@Override
	public String getDisplayName() {
		return "Convert selected paired producer-consumer arcs to read-arcs";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof STG;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualSTG stg = (VisualSTG)we.getModelEntry().getVisualModel();
		HashSet<Pair<VisualConnection /* consuming arc */, VisualConnection /* producing arc */>> dualArcs = new HashSet<>();
		// Filter consuming arcs that have dual producing arcs
		{
			HashSet<VisualConnection> consumingArcs = new HashSet<>(stg.getVisualConsumingArcs());
			if ( !stg.getSelection().isEmpty() ) {
				consumingArcs.retainAll(stg.getSelection());
			}
			for (VisualConnection consumingArc: consumingArcs) {
				Connection connection = stg.getConnection(consumingArc.getSecond(), consumingArc.getFirst());
				if ((connection instanceof VisualConnection) && !(connection instanceof VisualReadArc)) {
					VisualConnection producingArc = (VisualConnection)connection;
					dualArcs.add(new Pair<VisualConnection, VisualConnection>(consumingArc, producingArc));
				}
			}
		}
		// Filter producing arcs that have dual consuming arc
		{
			HashSet<VisualConnection> producerArcs = new HashSet<>(stg.getVisualProducerArcs());
			if ( !stg.getSelection().isEmpty() ) {
				producerArcs.retainAll(stg.getSelection());
			}
			for (VisualConnection producingArc: producerArcs) {
				Connection connection = stg.getConnection(producingArc.getSecond(), producingArc.getFirst());
				if ((connection instanceof VisualConnection) && !(connection instanceof VisualReadArc)) {
					VisualConnection consumingArc = (VisualConnection)connection;
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
				VisualReadArc readArc = StgTransformationUtils.convertDualArcToReadArc(stg, consumingArc, producingArc);
				if (readArc instanceof VisualReadArc) {
					readArcs.add(readArc);
				}
			}
			stg.select(new LinkedList<Node>(readArcs));
		}
	}

}
