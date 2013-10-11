package org.workcraft.plugins.son.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.VisualSuperGroup;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.util.GUI;

public class SelectionTool extends org.workcraft.gui.graph.tools.SelectionTool {

	private GraphEditorTool channelPlaceTool = null;
	private boolean asyn = true;
	private boolean sync = true;

	public SelectionTool(GraphEditorTool channelPlaceTool) {
		this.channelPlaceTool = channelPlaceTool;
	}

	@Override
	public void activated(GraphEditor editor) {
		super.activated(editor);
		createInterface();
	}

	private void createInterface() {
		JPanel sonPanel = new JPanel();
		controlPanel.add(sonPanel);
		JButton supergroupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/son-supergroup.svg"), "Merge selected nodes into supergroup (Ctrl+B)");
		supergroupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionSupergroup();
			}
		});
		sonPanel.add(supergroupButton);
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
					selectionLevelDown();
				}
				if(selectedNode instanceof VisualSuperGroup)
				{
					selectionLevelDown();
				}
				if(selectedNode instanceof VisualCondition){
					VisualCondition vc = (VisualCondition)selectedNode;
					if (vc.hasToken()==false)
							vc.setToken(true);
					else if (vc.hasToken()==true)
							vc.setToken(false);
				}

				if(selectedNode instanceof VisualChannelPlace){
					VisualChannelPlace cPlace = (VisualChannelPlace) node;
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
			// Note: level-up is handled in the parent
			// selectionLevelUp();
		}
		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					setChannelPlaceToolState(true);
					// Note: level-up is handled in the parent
					// selectionLevelUp();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					setChannelPlaceToolState(false);
					// Note: level-down is handled in the parent
					// selectionLevelDown();
					break;
				}
			}
		}

		if(e.isCtrlDown()){
			switch (e.getKeyCode()){
			case KeyEvent.VK_B:
				selectionSupergroup();
				break;
			}
		}
	}

	private void selectionSupergroup() {
		((VisualSON)getEditor().getModel()).superGroupSelection();
		getEditor().repaint();
	}

	private void setChannelPlaceToolState(boolean state) {
		getEditor().getMainWindow().getCurrentEditor().getToolBox().setToolButtonState(channelPlaceTool, state);
	}

}
