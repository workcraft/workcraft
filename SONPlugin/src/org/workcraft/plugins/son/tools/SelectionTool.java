package org.workcraft.plugins.son.tools;

import java.awt.event.KeyEvent;
import java.util.Collection;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;

public class SelectionTool extends org.workcraft.gui.graph.tools.SelectionTool{

	private GraphEditorTool channelPlaceTool = null;
	private boolean asyn = true;
	private boolean sync = true;

	public SelectionTool(GraphEditorTool channelPlaceTool) {
		this.channelPlaceTool = channelPlaceTool;
	}

	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		super.mouseClicked(e);
		VisualModel model = e.getEditor().getModel();

		if (e.getClickCount() > 1)
		{
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			Collection<Node> selection = e.getModel().getSelection();

			if(selection.size() == 1)
			{
				Node selectedNode = selection.iterator().next();

				if(selectedNode instanceof VisualONGroup)
				{
					setChannelPlaceToolState(false);
					currentLevelDown(e.getModel());
				}
				if(selectedNode instanceof VisualCondition){
					VisualPlace place = (VisualPlace) node;
					if (place.getTokens()==1)
						place.setTokens(0);
					else if (place.getTokens()==0)
						place.setTokens(1);
				}

				if(selectedNode instanceof VisualChannelPlace){
					VisualPlace cPlace = (VisualPlace) node;
					for (Connection con : model.getConnections(cPlace)){
						if (((VisualSONConnection) con).getSONConnectionType() == VisualSONConnection.SONConnectionType.ASYNLINE)
							this.sync = false;
						if (((VisualSONConnection) con).getSONConnectionType() == VisualSONConnection.SONConnectionType.SYNCLINE)
							this.asyn = false;
					}
					if (sync && !asyn)
						for (Connection con : model.getConnections(cPlace)){
						((VisualSONConnection) con).setSONConnectionType(VisualSONConnection.SONConnectionType.ASYNLINE);
						((VisualSONConnection) con).setMathConnectionType("ASYNLINE");
						}

					if (!sync && asyn)
						for (Connection con : model.getConnections(cPlace)){
						((VisualSONConnection) con).setSONConnectionType(VisualSONConnection.SONConnectionType.SYNCLINE);
						((VisualSONConnection) con).setMathConnectionType("SYNCLINE");
						}
					if (!sync && !asyn)
						for (Connection con : model.getConnections(cPlace)){
						((VisualSONConnection) con).setSONConnectionType(VisualSONConnection.SONConnectionType.SYNCLINE);
						((VisualSONConnection) con).setMathConnectionType("SYNCLINE");
						}
					asyn = true;
					sync = true;
				}

			}
		}
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e)
	{
		super.keyPressed(e);
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			setChannelPlaceToolState(true);
			currentLevelUp(e.getModel());
		}
		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					setChannelPlaceToolState(true);
					currentLevelUp(e.getModel());
					break;
				case KeyEvent.VK_PAGE_DOWN:
					setChannelPlaceToolState(false);
					currentLevelDown(e.getModel());
					break;
				}
			}
		}
	}

	private void setChannelPlaceToolState(boolean state) {
		editor.getMainWindow().getCurrentEditor().getToolBox().setToolButtonState(channelPlaceTool, state);
	}

}
