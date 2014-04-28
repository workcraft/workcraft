package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.components.VisualChannelPlace;
import org.workcraft.plugins.son.components.VisualCondition;
import org.workcraft.plugins.son.components.VisualEvent;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.util.GUI;

public class SelectionTool extends org.workcraft.gui.graph.tools.SelectionTool {

	private GraphEditorTool channelPlaceTool = null;
	private boolean asyn = true;
	private boolean sync = true;

	public SelectionTool(GraphEditorTool channelPlaceTool) {
		this.channelPlaceTool = channelPlaceTool;
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		interfacePanel = new JPanel(new BorderLayout());

		controlPanel = new JPanel(new WrapLayout(WrapLayout.CENTER, 0, 0));
		interfacePanel.add(controlPanel, BorderLayout.PAGE_START);

		JPanel groupPanel = new JPanel(new FlowLayout());
		controlPanel.add(groupPanel);


		JButton groupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-group.svg"), "Group selection (Ctrl+G)");
		groupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionGroup(editor);
			}
		});
		groupPanel.add(groupButton);

		//Create GroupPageButton
		JButton groupPageButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/page.svg"), "Group selection into a page/block (Alt+G)");

        //Create the popup menu.
        final JFrame frame = new JFrame();
        final JPopupMenu popup = new JPopupMenu();
        popup.add(new JMenuItem(new AbstractAction("Block") {
            public void actionPerformed(ActionEvent e) {
            	selectionBlock(editor);
            }
        }));
        popup.add(new JMenuItem(new AbstractAction("Page") {
            public void actionPerformed(ActionEvent e) {
				selectionPageGroup(editor);
            }
        }));
        groupPageButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });


		groupPanel.add(groupPageButton);

		JButton supergroupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/son-supergroup.svg"), "Merge selected nodes into supergroup (Ctrl+B)");
		supergroupButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionSupergroup(editor);
			}
		});
		groupPanel.add(supergroupButton);

		JButton ungroupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-ungroup.svg"), "Ungroup selection (Ctrl+Shift+G)");
		ungroupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionUngroup(editor);
			}
		});


		groupPanel.add(ungroupButton);

		JPanel levelPanel = new JPanel(new FlowLayout());
		controlPanel.add(levelPanel);
		JButton levelUpButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-level_up.svg"), "Level up (PageUp)");
		levelUpButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				changeLevelUp(editor);
			}
		});
		levelPanel.add(levelUpButton);


		JButton levelDownButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-level_down.svg"), "Level down (PageDown)");
		levelDownButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				changeLevelDown(editor);
			}
		});
		levelPanel.add(levelDownButton);

		JPanel flipPanel = new JPanel(new FlowLayout());
		controlPanel.add(flipPanel);
		JButton flipHorizontalButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-flip_horizontal.svg"), "Flip horizontal (Ctrl+F)");
		flipHorizontalButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionFlipHorizontal(editor);
			}
		});
		flipPanel.add(flipHorizontalButton);
		JButton flipVerticalButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-flip_vertical.svg"), "Flip vertical (Ctrl+Shift+F)");
		flipVerticalButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionFlipVertical(editor);
			}
		});
		flipPanel.add(flipVerticalButton);

		JPanel rotatePanel = new JPanel(new FlowLayout());
		controlPanel.add(rotatePanel);
		JButton rotateClockwiseButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-rotate_clockwise.svg"), "Rotate clockwise (Ctrl+R)");
		rotateClockwiseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionRotateClockwise(editor);
			}
		});
		rotatePanel.add(rotateClockwiseButton);
		JButton rotateCounterclockwiseButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-rotate_counterclockwise.svg"), "Rotate counterclockwise (Ctrl+Shift+R)");
		rotateCounterclockwiseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionRotateCounterclockwise(editor);
			}
		});
		rotatePanel.add(rotateCounterclockwiseButton);
	}


	@Override
	public void mouseClicked(GraphEditorMouseEvent e)
	{
		VisualModel model = e.getEditor().getModel();

		if (e.getClickCount() > 1)
		{
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			Collection<Node> selection = e.getModel().getSelection();

			if (model.getCurrentLevel() instanceof VisualGroup) {
				VisualGroup currentGroup = (VisualGroup)model.getCurrentLevel();
				if ( !currentGroup.getBoundingBoxInLocalSpace().contains(e.getPosition()) ) {
					setChannelPlaceToolState(e.getEditor(), true);
				}
			}

			if(selection.size() == 1)
			{
				Node selectedNode = selection.iterator().next();
				selectedNode = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);

				if (selectedNode instanceof VisualONGroup) {
					setChannelPlaceToolState(e.getEditor(), false);
				}
				if (selectedNode instanceof VisualCondition) {
					VisualCondition vc = (VisualCondition) selectedNode;
					if (vc.hasToken() == false)
						vc.setToken(true);
					else if (vc.hasToken() == true)
						vc.setToken(false);
				}

				if (selectedNode instanceof VisualEvent) {
					VisualEvent ve = (VisualEvent) selectedNode;
					if (ve.isFaulty() == false)
						ve.setFaulty(true);
					else if (ve.isFaulty() == true)
						ve.setFaulty(false);
				}

				if(selectedNode instanceof VisualChannelPlace) {
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
		super.mouseClicked(e);
	}

	@Override
	public void keyPressed(GraphEditorKeyEvent e)
	{
		super.keyPressed(e);
		if (!e.isCtrlDown())
		{
			if (!e.isShiftDown()) {
				switch (e.getKeyCode()) {
				case KeyEvent.VK_PAGE_UP:
					setChannelPlaceToolState(e.getEditor(), true);
					// Note: level-up is handled in the parent
					// selectionLevelUp();
					break;
				case KeyEvent.VK_PAGE_DOWN:
					setChannelPlaceToolState(e.getEditor(), false);
					// Note: level-down is handled in the parent
					// selectionLevelDown();
					break;
				}
			}
		}

		if(e.isCtrlDown()){
			switch (e.getKeyCode()){
			case KeyEvent.VK_B:
				selectionSupergroup(e.getEditor());
				break;
			}
		}
	}

	private void selectionSupergroup(final GraphEditor editor) {
		((VisualSON)editor.getModel()).superGroupSelection();
		editor.repaint();
	}

	private void selectionBlock(final GraphEditor editor) {
		((VisualSON)editor.getModel()).groupBlockSelection();
		editor.repaint();
	}

	private void setChannelPlaceToolState(final GraphEditor editor, boolean state) {
		editor.getMainWindow().getCurrentEditor().getToolBox().setToolButtonState(channelPlaceTool, state);
	}

}
