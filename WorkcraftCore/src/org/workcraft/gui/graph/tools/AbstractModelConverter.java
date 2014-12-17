package org.workcraft.gui.graph.tools;

import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.util.Hierarchy;

public abstract class AbstractModelConverter<TSrcModel extends VisualModel, TDstModel extends VisualModel> {
	final private TSrcModel srcModel;
	final private TDstModel dstModel;
	final private HashMap<String, Container> containers;
	final HashMap<VisualComponent, VisualComponent> dst2src;

	public AbstractModelConverter(TSrcModel srcModel, TDstModel dstModel) {
		this.srcModel = srcModel;
		this.dstModel = dstModel;
		this.containers = NamespaceHelper.copyPageStructure(dstModel, dstModel.getRoot(), srcModel, srcModel.getRoot(), null);
		this.dst2src = new HashMap<>();
		convertComponents();
		convertConnections();
		convertGroups();
	}

	public abstract void convertComponents();

	public void convertConnections() {
		for(VisualConnection connection : Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualConnection.class)) {
			VisualComponent first = connection.getFirst();
			VisualComponent second = connection.getSecond();
			VisualComponent newFirst = dst2src.get(first);
			VisualComponent newSecond= dst2src.get(second);
			if ((newFirst != null) && (newSecond != null)) {
				try {
					VisualConnection newConnection = dstModel.connect(newFirst, newSecond);
					newConnection.copyStyle(connection);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void convertGroups() {
		for(VisualGroup pnGroup: Hierarchy.getDescendantsOfType(srcModel.getRoot(), VisualGroup.class)) {
			HashSet<Node> stgSelection = new HashSet<>();
			for (Node pnNode: pnGroup.getChildren()) {
				Node stgNode = dst2src.get(pnNode);
				if (stgNode != null) {
					stgSelection.add(stgNode);
				}
			}
			if ( !stgSelection.isEmpty() ) {
				dstModel.addToSelection(stgSelection);
				VisualGroup stgGroup = dstModel.groupSelection();
				stgGroup.copyStyle(pnGroup);
			}
		}
		dstModel.selectNone();
	}

	public TSrcModel getSrcModel() {
		return srcModel;
	}

	public TDstModel getDstModel() {
		return dstModel;
	}

}
