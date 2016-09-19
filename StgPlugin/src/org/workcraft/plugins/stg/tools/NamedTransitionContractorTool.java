package org.workcraft.plugins.stg.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.petri.tools.TransitionContractorTool;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgPlace;
import org.workcraft.plugins.stg.VisualImplicitPlaceArc;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.workspace.WorkspaceEntry;

public class NamedTransitionContractorTool extends TransitionContractorTool {

    HashSet<VisualPlace> convertedImplicitPlaces = new HashSet<>();

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Stg;
    }

    @Override
    public void beforeContraction(VisualModel visualModel, VisualTransition visualTransition) {
        super.beforeContraction(visualModel, visualTransition);
        convertedImplicitPlaces.clear();
        if (visualModel instanceof VisualStg) {
            VisualStg visualStg = (VisualStg) visualModel;
            Set<Connection> adjacentConnections = new HashSet<>(visualModel.getConnections(visualTransition));
            for (Connection connection: adjacentConnections) {
                if (connection instanceof VisualImplicitPlaceArc) {
                    VisualPlace formerImplicitPlace = visualStg.makeExplicit((VisualImplicitPlaceArc) connection);
                    convertedImplicitPlaces.add(formerImplicitPlace);
                }
            }
        }
    }

    @Override
    public void afterContraction(VisualModel visualModel, VisualTransition visualTransition,
            HashMap<VisualPlace, Pair<VisualPlace, VisualPlace>> productPlaceMap) {
        super.afterContraction(visualModel, visualTransition, productPlaceMap);
        if (visualModel instanceof VisualStg) {
            VisualStg visualStg = (VisualStg) visualModel;
            for (VisualPlace productPlace: productPlaceMap.keySet()) {
                Pair<VisualPlace, VisualPlace> originalPlacePair = productPlaceMap.get(productPlace);
                VisualPlace predPlace = originalPlacePair.getFirst();
                VisualPlace succPlace = originalPlacePair.getSecond();
                if (convertedImplicitPlaces.contains(predPlace) && convertedImplicitPlaces.contains(succPlace)) {
                    VisualConnection connection = visualStg.maybeMakeImplicit(productPlace, true);
                    filterControlPoints(connection);
                }
            }
        }
    }

    @Override
    public VisualPlace createProductPlace(VisualModel visualModel, VisualPlace predPlace, VisualPlace succPlace) {
        Container visualContainer = (Container) Hierarchy.getCommonParent(predPlace, succPlace);
        Container mathContainer = NamespaceHelper.getMathContainer(visualModel, visualContainer);
        MathModel mathModel = visualModel.getMathModel();
        HierarchicalUniqueNameReferenceManager refManager = (HierarchicalUniqueNameReferenceManager) mathModel.getReferenceManager();
        NameManager nameManagerer = refManager.getNameManager((NamespaceProvider) mathContainer);
        String predName = visualModel.getMathName(predPlace);
        String succName = visualModel.getMathName(succPlace);
        String productName = nameManagerer.getDerivedName(null, predName + succName);
        StgPlace mathPlace = mathModel.createNode(productName, mathContainer, StgPlace.class);
        return visualModel.createVisualComponent(mathPlace, visualContainer, VisualPlace.class);
    }

}
