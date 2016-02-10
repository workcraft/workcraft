package org.workcraft.gui.graph.tools;

import java.util.Map;

import org.workcraft.dom.Container;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualReplica;
import org.workcraft.dom.visual.connections.VisualConnection;

public interface ModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel>  {

    TSrcModel getSrcModel();
    TDstModel getDstModel();
    Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap();
    Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap();

    String convertNodeName(String srcName, Container container);
    Container getRefToDstPage(String ref);
    VisualNode getSrcToDstNode(VisualNode srcNode);

    void preprocessing();
    void postprocessing();

    VisualPage convertPage(VisualPage srcPage);
    VisualComponent convertComponent(VisualComponent srcComponent);
    VisualReplica convertReplica(VisualReplica srcReplica);
    VisualGroup convertGroup(VisualGroup srcGroup);
    VisualConnection convertConnection(VisualConnection srcConnection);

}
