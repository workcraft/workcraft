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

	public TSrcModel getSrcModel();
	public TDstModel getDstModel();
	public Map<Class<? extends MathNode>, Class<? extends MathNode>> getComponentClassMap();
	public Map<Class<? extends VisualReplica>, Class<? extends VisualReplica>> getReplicaClassMap();

	public String convertNodeName(String srcName, Container container);
	public Container getRefToDstPage(String ref);
	public VisualNode getSrcToDstNode(VisualNode srcNode);

	public void preprocessing();
	public void postprocessing();

	public VisualPage convertPage(VisualPage srcPage);
	public VisualComponent convertComponent(VisualComponent srcComponent);
	public VisualReplica convertReplica(VisualReplica srcReplica);
	public VisualGroup convertGroup(VisualGroup srcGroup);
	public VisualConnection convertConnection(VisualConnection srcConnection);

}
