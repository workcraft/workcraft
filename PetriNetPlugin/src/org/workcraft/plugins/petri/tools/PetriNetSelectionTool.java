package org.workcraft.plugins.petri.tools;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPopupMenu;

import org.workcraft.Tool;
import org.workcraft.TransformationTool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.actions.ToolAction;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.Tools;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriNetSelectionTool extends SelectionTool {

	public PetriNetSelectionTool() {
		super();
	}

	public PetriNetSelectionTool(boolean enablePages) {
		super(enablePages);
	}
	@Override
	public void mouseClicked(GraphEditorMouseEvent e) {
		boolean processed = false;
		VisualModel model = e.getEditor().getModel();
		VisualNode node = (VisualNode)HitMan.hitTestForSelection(e.getPosition(), model);
		if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
			JPopupMenu popup = createPopupMenu(node, e.getEditor());
			if (popup != null) {
				if (node == null) {
					model.selectNone();
				} else {
					model.select(node);
				}
				MouseEvent systemEvent = e.getSystemEvent();
				popup.show(systemEvent.getComponent(), systemEvent.getX(), systemEvent.getY());
			}
			processed = true;
		} else if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
			if(node instanceof VisualPlace) {
				VisualPlace place = (VisualPlace) node;
				if (place.getReferencedPlace().getTokens() <= 1) {
					e.getEditor().getWorkspaceEntry().saveMemento();

					if (place.getReferencedPlace().getTokens()==1) {
						place.getReferencedPlace().setTokens(0);
					} else {
						place.getReferencedPlace().setTokens(1);
					}
				}
				processed = true;
			}
		}

		if (!processed) {
			super.mouseClicked(e);
		}
	}

	private JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
		JPopupMenu popup = null;
		WorkspaceEntry we = editor.getWorkspaceEntry();
		List<Tool> applicableTools = new ArrayList<>();
		for (Tool tool: Tools.getApplicableTools(we)) {
			if (tool instanceof TransformationTool) {
				TransformationTool transformTool = (TransformationTool)tool;
				if (transformTool.isApplicableToNode(node)) {
					applicableTools.add(transformTool);
				}
			}
		}
		if ( !applicableTools.isEmpty() ) {
			popup = new JPopupMenu();
			popup.setFocusable(false);
			MainWindow mainWindow = editor.getMainWindow();
			for (Tool tool: applicableTools) {
				ToolAction toolAction = new ToolAction(tool);
				ActionMenuItem miTool = new ActionMenuItem(toolAction);
				miTool.addScriptedActionListener(mainWindow.getDefaultActionListener());
				popup.add(miTool);
			}
		}
		return popup;
	}

}
