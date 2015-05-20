package org.workcraft.plugins.son.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class SONSelectionTool extends SelectionTool {

	private GraphEditorTool channelPlaceTool = null;
	private boolean asyn = true;
	private boolean sync = true;

	public SONSelectionTool(GraphEditorTool channelPlaceTool) {
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

		//Create groupButton
		final JButton groupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/selection-group.svg"), "Group selection (Ctrl+G)");
		groupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionGroup(editor);
			}
		});
		groupPanel.add(groupButton);

/*		//Create superGroupButton
		final JButton superGroupButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/son-super-group.svg"), "Super group selection (Gtrl+V)");
		superGroupButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionSupergroup(editor);
			}
		});
		groupPanel.add(superGroupButton);*/

		//Create blockButton
		JButton blockButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/son-block.svg"), "Group selection into a block (Alt+B)");
		blockButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionBlock(editor);
			}
		});

		groupPanel.add(blockButton);

		//Create pageButton
		JButton groupPageButton = GUI.createIconButton(GUI.createIconFromSVG(
				"images/icons/svg/page.svg"), "Group selection into a page (Alt+G)");
		groupPageButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				selectionPageGroup(editor);
			}
		});

		groupPanel.add(groupPageButton);

		//Create ungroupButton
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
	public void mouseClicked(GraphEditorMouseEvent e){
		VisualSON model = (VisualSON)e.getEditor().getModel();

		if (e.getClickCount() > 1)
		{
			VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
			Collection<Node> selection = e.getModel().getSelection();

			if(selection.size() == 1)
			{
				Node selectedNode = selection.iterator().next();
				selectedNode = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);

				if (selectedNode instanceof VisualBlock) {
					if(!((VisualBlock)selectedNode).getIsCollapsed())
						((VisualBlock)selectedNode).setIsCollapsed(true);
					else
						((VisualBlock)selectedNode).setIsCollapsed(false);

					return;
				}

				if (selectedNode instanceof VisualCondition) {
					VisualCondition vc = (VisualCondition) selectedNode;
					if (vc.isMarked() == false)
						vc.setMarked(true);
					else if (vc.isMarked() == true)
						vc.setMarked(false);
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

						if (((VisualSONConnection) con).getSemantics() == Semantics.ASYNLINE)
							this.sync = false;
						if (((VisualSONConnection) con).getSemantics() == Semantics.SYNCLINE)
							this.asyn = false;
					}
					if (sync && !asyn)
						for (Connection con : model.getConnections(cPlace)){
							((VisualSONConnection) con).setSemantics(Semantics.ASYNLINE);
						}

					if (!sync && asyn)
						for (Connection con : model.getConnections(cPlace)){
							((VisualSONConnection) con).setSemantics(Semantics.SYNCLINE);
						}
					if (!sync && !asyn)
						for (Connection con : model.getConnections(cPlace)){
							((VisualSONConnection) con).setSemantics(Semantics.SYNCLINE);
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

		if (e.isAltDown() && !e.isCtrlDown() && !e.isShiftDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_B:
				selectionBlock(e.getEditor());
				break;
			}
		}
	}

	@Override
	protected void changeLevelDown(final GraphEditor editor) {
		VisualModel model = editor.getModel();
		Collection<Node> selection = model.getSelection();
		if (selection.size() == 1) {
			Node node = selection.iterator().next();
			if(node instanceof Container && !(node instanceof VisualBlock)) {
				model.setCurrentLevel((Container)node);
				if(node instanceof VisualONGroup)
					setChannelPlaceToolState(editor, false);
				else
					setChannelPlaceToolState(editor, true);
				editor.repaint();
			}
		}
	}

	@Override
	protected void changeLevelUp(final GraphEditor editor) {
		VisualModel model = editor.getModel();
		Container level = model.getCurrentLevel();
		Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
		if (parent != null && !(level instanceof VisualBlock)) {
			model.setCurrentLevel(parent);
			if(parent instanceof VisualONGroup)
				setChannelPlaceToolState(editor, false);
			else
				setChannelPlaceToolState(editor, true);
			model.addToSelection(level);
			editor.repaint();
		}
	}

	private void selectionBlock(final GraphEditor editor) {
		((VisualSON)editor.getModel()).groupBlockSelection();
		editor.repaint();
	}

	private void setChannelPlaceToolState(final GraphEditor editor, boolean state) {
		editor.getMainWindow().getCurrentEditor().getToolBox().setToolButtonState(channelPlaceTool, state);
	}

}
