package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.petri.converters.DefaultPetriConverter;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.Pair;

import java.util.Map;

public class StgToPetriConverter extends DefaultPetriConverter<VisualStg, VisualPetri> {

    public StgToPetriConverter(VisualStg srcModel) {
        super(srcModel, new VisualPetri(new Petri()));
    }

    @Override
    public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap() {
        Map<Class<? extends MathNode>, Class<? extends MathNode>> result = super.getComponentClassMap();
        result.put(StgPlace.class, Place.class);
        result.put(DummyTransition.class, Transition.class);
        result.put(SignalTransition.class, Transition.class);
        return result;
    }

    @Override
    public String convertNodeName(String srcName, Container container, Class<? extends MathNode> srcNodeClass) {
        if (srcNodeClass.isAssignableFrom(DummyTransition.class) || srcNodeClass.isAssignableFrom(SignalTransition.class)) {
            Pair<String, Integer> instancedTransition = LabelParser.parseInstancedTransition(srcName);
            String dstCandidate = instancedTransition.getFirst()
                    .replace("+", "_PLUS")
                    .replace("-", "_MINUS")
                    .replace("~", "_TOGGLE");

            HierarchyReferenceManager refManager = getDstModel().getMathModel().getReferenceManager();
            NamespaceProvider namespaceProvider = refManager.getNamespaceProvider(container);
            NameManager nameManager = refManager.getNameManager(namespaceProvider);
            return nameManager.getDerivedName(null, dstCandidate);
        }
        return super.convertNodeName(srcName, container, srcNodeClass);
    }

    @Override
    public void preprocessing() {
        VisualStg stg = getSrcModel();
        for (VisualImplicitPlaceArc connection: stg.getVisualImplicitPlaceArcs()) {
            stg.makeExplicit(connection);
        }
    }

}
