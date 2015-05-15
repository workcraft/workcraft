package org.workcraft.plugins.stg.tools;

import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.gui.graph.tools.DefaultModelConverter;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.LabelParser;
import org.workcraft.plugins.stg.STGPlace;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualSTG;

public 	class StgToPetriNetConverter extends DefaultModelConverter<VisualSTG, VisualPetriNet>  {

	public StgToPetriNetConverter(VisualSTG srcModel, VisualPetriNet dstModel) {
		super(srcModel, dstModel);
	}

	@Override
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
		Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
		result.put(STGPlace.class, Place.class);
		result.put(DummyTransition.class, Transition.class);
		result.put(SignalTransition.class, Transition.class);
		return result;
	}

	@Override
	public String convertNodeName(String srcName, Container container) {
		String dstCandidate = LabelParser.getTransitionName(srcName);
		dstCandidate = dstCandidate.replace("+", "_PLUS").replace("-", "_MINUS").replace("~", "_TOGGLE");

		HierarchicalUniqueNameReferenceManager refManager
			= (HierarchicalUniqueNameReferenceManager)getDstModel().getPetriNet().getReferenceManager();

		NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(container);
		NameManager nameManagerer = refManager.getNameManager(namespaceProvider);
		return nameManagerer.getDerivedName(null, dstCandidate);
	}

	@Override
	public void preprocessing() {
		VisualSTG stg = getSrcModel();
		for (VisualImplicitPlaceArc connection: stg.getVisualImplicitPlaceArcs()) {
			stg.makeExplicit(connection);
		}
	}

}