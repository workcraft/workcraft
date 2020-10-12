package org.workcraft.plugins.petri.converters;

import org.workcraft.dom.converters.DefaultModelConverter;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualReplicaPlace;

import java.util.Map;

public class DefaultPetriConverter<S extends VisualModel, T extends VisualModel> extends DefaultModelConverter<S, T> {

    public DefaultPetriConverter(S srcModel, T dstModel) {
        super(srcModel, dstModel);
    }

    @Override
    public Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap() {
        Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> result = super.getReplicaClassMap();
        result.put(VisualReplicaPlace.class, VisualReplicaPlace.class);
        return result;
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
