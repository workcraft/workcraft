package org.workcraft.plugins.petri.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;

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
			model.select(node);
			JPopupMenu popup = createPopupMenu(node, e.getEditor());
			if (popup != null) {
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
		JPopupMenu popup = new JPopupMenu();

		if (node instanceof VisualTransition) {
			final VisualTransition transition= (VisualTransition)node;
			popup.setFocusable(false);
			popup.add(new JLabel("Transition"));
			popup.addSeparator();
			{
				JMenuItem contractionMenuItem = new JMenuItem("Contract transition");
				contractionMenuItem.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						TransitionContractorTool.transform(editor.getWorkspaceEntry());
					}
				});
				popup.add(contractionMenuItem);
			}
			return popup;
		}

		return null;
	}


}
