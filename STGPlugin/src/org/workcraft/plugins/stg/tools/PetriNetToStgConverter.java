package org.workcraft.plugins.stg.tools;

import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.VisualDummyTransition;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.plugins.stg.VisualSignalTransition;

public class PetriNetToStgConverter extends DefaultModelConverter<VisualPetriNet, VisualSTG> {

	public PetriNetToStgConverter(VisualPetriNet srcModel, VisualSTG dstModel) {
		super(srcModel, dstModel);
	}

	@Override
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
		result.put(Place.class, STGPlace.class);
		result.put(Transition.class, DummyTransition.class);
		return result;
	}

	@Override
	public Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap() {
		Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> result = super.getReplicaClassMap();
		result.put(VisualReplicaPlace.class, VisualReplicaPlace.class);
		return result;
	}

	@Override
	public VisualComponent convertComponent(VisualComponent srcComponent) {
		VisualComponent dstComponent = super.convertComponent(srcComponent);
		if ( (dstComponent instanceof VisualDummyTransition) || (dstComponent instanceof VisualSignalTransition) ) {
			dstComponent.setLabel("");
		}
		return dstComponent;
	}

	@Override
	public VisualConnection convertConnection(VisualConnection srcConnection) {
		VisualConnection dstConnection = null;
		if (srcConnection instanceof VisualReadArc) {
			VisualNode srcFirst = srcConnection.getFirst();
			VisualNode srcSecond = srcConnection.getSecond();
			VisualNode dstFirst = getSrcToDstNode(srcFirst);
			VisualNode dstSecond = getSrcToDstNode(srcSecond);
			if ((dstFirst != null) && (dstSecond != null)) {
				try {
					dstConnection = getDstModel().connectUndirected(dstFirst, dstSecond);
					dstConnection.copyStyle(srcConnection);
					dstConnection.copyShape(srcConnection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}

		} else {
			dstConnection = super.convertConnection(srcConnection);
		}
		return dstConnection;
	}

}
