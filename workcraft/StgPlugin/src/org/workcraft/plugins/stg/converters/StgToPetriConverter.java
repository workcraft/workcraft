package org.workcraft.plugins.stg.converters;

import org.workcraft.dom.Container;
import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchyReferenceManager;
import org.workcraft.dom.references.NameManager;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.*;
import org.workcraft.plugins.stg.*;
import org.workcraft.plugins.stg.utils.LabelParser;
import org.workcraft.types.Pair;

import java.util.Map;

public class StgToPetriConverter extends DefaultModelConverter<VisualStg, VisualPetri> {

    public StgToPetriConverter(VisualStg srcModel, VisualPetri dstModel) {
        super(srcModel, dstModel);
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
    public Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap() {
        Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> result = super.getReplicaClassMap();
        result.put(VisualReplicaPlace.class, VisualReplicaPlace.class);
        return result;
    }

    @Override
    public String convertNodeName(String srcName, Container container) {
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

    @Override
    public void preprocessing() {
        VisualStg stg = getSrcModel();
        for (VisualImplicitPlaceArc connection: stg.getVisualImplicitPlaceArcs()) {
            stg.makeExplicit(connection);
        }
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
